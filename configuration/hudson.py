#!/usr/bin/env python3
#
# Fetches and analyzes build and test data from the Hudson API
# Run with -h or --help for usage information
# @author Tim Hessenmüller (D062243)
#
try:
	import aiohttp
except ImportError:
	print("Missing package: aiohttp\nInstall with: pip3 install aiohttp")
	exit(1)
import asyncio, logging, random
from typing import List, Tuple, Dict
from argparse import ArgumentParser
from getpass import getpass

# ---------- Model ----------

class WithUrl:
	url: str

	def __init__(self, url):
		self.url = url

class NamedWithUrl(WithUrl):
	name: str

	def __init__(self, name, url):
		super(NamedWithUrl, self).__init__(url)
		self.name = name


class Case:
	name: str
	status: str
	duration: int
	fail_age: int

	def __init__(self, name: str, status: str, duration: int, fail_age: int):
		self.name = name
		self.status = status
		self.duration = duration
		self.fail_age = fail_age

class Suite:
	name: str
	cases: List[Case] = []
	duration: float

	def __init__(self, name: str, duration: float):
		self.name = name
		self.duration = duration

class Build(NamedWithUrl):
	suites: List[Suite] = []

class Job(NamedWithUrl):
	builds: List[Build] = []
	color: str

class Root(WithUrl):
	jobs: List[Job] = []

# ---------- Donwloader ---------

class Downloader:
	__session: aiohttp.ClientSession
	__lock: asyncio.Semaphore
	__all_job_urls: Dict[str, str] = None
	base_url: str
	api_suffix: str

	logger = logging.getLogger(__name__)

	n_builds = 0
	p_builds = 0

	def __init__(self, base_url: str, auth: Tuple[str, str], max_concurrent_connections, api_suffix="api/json"):
		self.base_url = base_url
		self.__session = aiohttp.ClientSession(auth=aiohttp.BasicAuth(auth[0], auth[1]))
		self.__lock = asyncio.Semaphore(max_concurrent_connections)
		self.api_suffix = api_suffix
	
	async def close(self):
		if self.__session != None:
			await self.__session.close()
	
	async def build_tree(self, jobs: List[str], build_limit: int = 0) -> Root:
		if self.__all_job_urls == None:
			# Very first request; Test connection and get all existing jobs to compare against
			try:
				await self.__get_job_list()
			except aiohttp.ClientResponseError as e:
				self.logger.critical("Connection to Hudson failed:\n" + e)
				exit(78)
		
		root = Root(self.base_url)
		for job in jobs:
			if job == "*":
				root.jobs = [Job(k, v) for k, v in self.__all_job_urls.items()]
				break
			elif job in self.__all_job_urls:
				root.jobs.append(Job(job, self.__all_job_urls.get(job)))
			else:
				self.logger.warning("Could not find job: " + job)

		await asyncio.gather(*[self.__populate_builds_in_job(job, build_limit) for job in root.jobs])
		return root
	
	async def __get_job_list(self) -> None:
		self.__all_job_urls = {}
		json = await self.get_json(self.base_url)
		for job in json["jobs"]:
			self.__all_job_urls[job["name"]] = job["url"]
	
	async def __populate_builds_in_job(self, job: Job, build_limit: int) -> None:
		try:
			json = await self.get_json(job.url)
		except aiohttp.ClientResponseError:
			return
		job.color = json.get("color")
		if build_limit <= 0:
			for build in json["builds"]:
				job.builds.append(Build(str(build["number"]), build["url"]))
		else:
			for build in json["builds"][0:build_limit]:
				job.builds.append(Build(str(build["number"]), build["url"]))
		self.n_builds += len(job.builds)
		await asyncio.gather(*[self.__populate_suites_in_build(build) for build in job.builds])
	
	async def __populate_suites_in_build(self, build: Build) -> None:
		try:
			json = await self.get_json(build.url + "testReport/api/json", append_api_suffix=False)
		except aiohttp.ClientResponseError:
			return
		build.suites = await asyncio.gather(*[self.__parse_suite_with_cases(suite) for suite in json["suites"]])
		self.p_builds += 1
	
	async def __parse_suite_with_cases(self, json: str) -> Suite:
		suite = Suite(json.get("name"), json.get("duration"))
		suite.cases = [] # For some reason new Suite instances all share the same list
		for case_json in json["cases"]:
			suite.cases.append(Case(case_json["className"], case_json["status"], case_json["duration"], case_json["age"]))
		return suite
	
	async def get_json(self, url: str, append_api_suffix=True):
		if append_api_suffix == True:
			if not url.endswith("/"):
				url += "/"
			url += self.api_suffix
		async with self.__lock:
			for _ in range(5):
				try:
					async with self.__session.request("GET", url) as resp:
						try:
							resp.raise_for_status()
						except aiohttp.ClientResponseError as e:
							self.logger.debug(e)
							raise e
						return await resp.json(content_type=None) # Disables content type check
				except asyncio.TimeoutError as e:
					self.logger.warning(e)
					await asyncio.sleep(random.uniform(1, 10))
			self.logger.error(f"5 consecutive timeouts for URL: {url}")
			raise aiohttp.ClientResponseError
	
	def get_progress(self):
		if self.n_builds == 0:
			return 0
		return self.p_builds / self.n_builds

# ---------- Analysis ----------

class AbstractModule:
	def process(self, root: Root):
		raise NotImplementedError
	def progress(self):
		raise NotImplementedError
	def result(self) -> str:
		return None

class Analyzer:
	__tasks = []
	modules: List[AbstractModule]

	def __init__(self, *modules: AbstractModule):
		self.modules = list(modules)
	
	async def analyze_tree(self, root: Root):
		loop = asyncio.get_event_loop()
		await asyncio.gather(*[loop.run_in_executor(None, m.process, root) for m in self.modules])
	
	def get_progress(self):
		if len(self.modules) == 0:
			return 0
		acc = 0
		for m in self.modules:
			acc += m.progress()
		return acc / len(self.modules)
	
	def print_results(self):
		for m in self.modules:
			r = m.result()
			if r != None:
				print(f"Results from {m.__class__.__name__}:\n{r}")

	def add_module(self, module: AbstractModule):
		self.modules.append(module)
	
	def remove_module(self, module: AbstractModule):
		self.modules.remove(module)


class ModelVisualizerModule(AbstractModule):
	__root: Root
	level: int
	def __init__(self, level: int = 1):
		super(ModelVisualizerModule, self).__init__()
		self.level = level
	def process(self, root: Root):
		self.__root = root
	def progress(self):
		return 1
	def result(self):
		out = ""
		for job in self.__root.jobs:
			out += job.name + ": " + job.color + ", " + str(len(job.builds)) + " builds\n"
			if self.level >= 1:
				for build in job.builds:
					out += "  " + build.name + ": " + str(len(build.suites)) + " test suites\n"
					if self.level >= 2:
						for suite in build.suites:
							out += "    " + suite.name + ": " + str(len(suite.cases)) + " test cases\n"
		return out[:-1]

class FlakyCasesModule(AbstractModule):
	threshold_div: int
	case_counter: Dict[str, int] = {}
	n_builds = 0
	p_builds = 0

	def __init__(self, threshold_div: int = 4):
		super(FlakyCasesModule, self).__init__()
		self.threshold_div = threshold_div
	
	def process(self, root: Root):
		for job in root.jobs:
			self.n_builds += len(job.builds)
		
		for job in root.jobs:
			for build in job.builds:
				for suite in build.suites:
					for case in suite.cases:
						if case.fail_age == 1:
							self.case_counter[case.name] = self.case_counter.get(case.name, 0) + 1
				self.p_builds += 1
	
	def progress(self):
		if self.n_builds == 0:
			return 0
		return self.p_builds / self.n_builds
	
	def result(self):
		out = ""
		s = [(k, self.case_counter.get(k)) for k in sorted(self.case_counter, key=self.case_counter.get, reverse=True)]
		for case, new_fails in s:
			if (new_fails <= self.n_builds // 4):
				break
			out += f"  {new_fails / self.n_builds:.2f}, {case}\n"
		return out[:-1]

class FailedCasesModule(AbstractModule):
	case_counter: Dict[str, int] = {}
	n_builds = 0
	p_builds = 0

	def __init__(self):
		super(FailedCasesModule, self).__init__()

	def process(self, root: Root):
		for job in root.jobs:
			self.n_builds += len(job.builds)

		for job in root.jobs:
			for build in job.builds:
				for suite in build.suites:
					for case in suite.cases:
						if case.status == "FAILED" or case.status == "REGRESSION":
							self.case_counter[case.name] = self.case_counter.get(case.name, 0) + 1
				self.p_builds += 1
	
	def progress(self):
		if self.n_builds == 0:
			return 0
		return self.p_builds / self.n_builds

	def result(self):
		out = ""
		s = [(k, self.case_counter.get(k)) for k in sorted(self.case_counter, key=self.case_counter.get, reverse=True)]
		for case, fails in s:
			out += f"{fails:>5}, {case}\n"
		return out[:-1]

class FailedSuitesModule(FailedCasesModule):
	def __init__(self):
		super(FailedSuitesModule, self).__init__()

	def process(self, root: Root):
		for job in root.jobs:
			self.n_builds += len(job.builds)

		for job in root.jobs:
			for build in job.builds:
				for suite in build.suites:
					for case in suite.cases:
						if case.status == "FAILED":
							self.case_counter[suite.name] = self.case_counter.get(suite.name, 0) + 1
							break
				self.p_builds += 1

class CaseDurationModule(AbstractModule):
	dur_counter = {}
	run_counter = {}
	n_builds = 0
	p_builds = 0
	def __init__(self):
		super(CaseDurationModule, self).__init__()

	def process(self, root: Root):
		for job in root.jobs:
			self.n_builds += len(job.builds)

		for job in root.jobs:
			for build in job.builds:
				for suite in build.suites:
					for case in suite.cases:
						if case.status == "PASSED":
							self.dur_counter[case.name] = self.dur_counter.get(case.name, 0) + case.duration
							self.run_counter[case.name] = self.run_counter.get(case.name, 0) + 1
				self.p_builds += 1

	def progress(self):
		if self.n_builds == 0:
			return 0
		return self.p_builds / self.n_builds
	
	def result(self):
		out = ""
		s = [(case, total / self.run_counter.get(case), total) for case, total in self.dur_counter.items()]
		s = sorted(s, key=lambda x: x[1], reverse=True)
		for case, duration, total in s:
			out += f"{duration:>3}s/r ({total:>6}s total): {case}\n"
		return out[:-1]

# ---------- Functions And Helpers ----------

class ProgressBar:
	__task = None
	enabled = True
	running = False
	__spin = -1
	def start(self, progress_function):
		if self.__task == None:
			self.__task = asyncio.create_task(self.show(progress_function))
	async def show(self, progress_function):
		if not self.enabled:
			return
		self.running = True
		while self.running:
			p = progress_function()
			print(f"{str(round(p * 100)):>3}% [{self.__bar(p)}] {self.__spinner()}", end="\r", flush=True)
			if p >= 1:
				self.running = False
			else:
				await asyncio.sleep(0.5)
		print(f"100% [{self.__bar(1)}]  ", flush=True)
	def __bar(self, p):
		width = 20
		pos = round(p * width)
		if pos > 0:
			out = f"{'=' * (pos - 1)}>"
		else:
			out = ""
		return out + "-" * (width - len(out))
	def __spinner(self):
		seq = "-\\|/"
		self.__spin = (self.__spin + 1) % len(seq)
		return seq[self.__spin]
	async def done(self):
		self.running = False
		if self.__task != None:
			await self.__task
			self.__task = None

def parse_args():
	parser = ArgumentParser(description="Fetches and analyzes build and test data from the Hudson API")
	parser.add_argument("username", help="Hudson account username")
	parser.add_argument("job", nargs="+", help="one or more jobs to fetch")
	parser.add_argument("--host", default="https://hudson.sapsailing.com/", help="hostname / IP address of Hudson instance")
	parser.add_argument("-c", type=int, default=25, metavar="NUM", help="maximum concurrent connections")
	parser.add_argument("-n", type=int, default=0, metavar="LIMIT", help="limits the number of builds per job")
	v_group = parser.add_mutually_exclusive_group()
	v_group.add_argument("-v", action="store_true")
	v_group.add_argument("-q", action="store_true")
	args = parser.parse_args()
	if not args.host.endswith("/"):
		args.host += "/"
	return args

def create_logger(args):
	level = logging.INFO
	if args.q:
		level = logging.ERROR
	elif args.v:
		level = logging.DEBUG
	log_handler = logging.StreamHandler()
	log_handler.setFormatter(logging.Formatter("%(asctime)s [%(levelname)s] %(message)s", datefmt="%Y-%m-%dT%H:%M:%S%z"))
	log_handler.setLevel(level)
	logger = logging.getLogger(__name__)
	logger.addHandler(log_handler)
	logger.setLevel(level)
	return logger

def get_credentials(username):
	password = getpass(f"Enter password: ")
	return (username, password)

async def main(args):
	logger = create_logger(args)

	pb = ProgressBar()
	pb.enabled = not args.q
	d = Downloader(args.host, get_credentials(args.username), args.c)
	logger.info("Fetching data...")
	pb.start(d.get_progress)
	tree = await d.build_tree(args.job, args.n)
	await d.close()
	await pb.done()
	logger.info("Processing data...")
	a = Analyzer(ModelVisualizerModule(level=1), FailedCasesModule(), FlakyCasesModule(threshold_div=4))
	pb.start(a.get_progress)
	await a.analyze_tree(tree)
	await pb.done()
	logger.info("Outputting results...")
	a.print_results()

if __name__ == "__main__":
	asyncio.run(main(parse_args()))
