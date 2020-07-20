# SAP Sailing Analytics on Kubernetes
## 1. Objectives
Create a cluster with a running Sailing Analytics Master Replica Set and all needed central services: rabbitMQ, MongoDB.

After that think of a CustomResourceDefinition to model an event accordingly and implement a controller that handles those CRDs accordingly by creating/modifying deployments/resources.

To create a controller we can use kubebuilder[2].

### a) Mongodb
Create a mongodb replicaset, with the following key features:
- reproducable deployments
- Smooth handling of node and pod failures
- Antiaffinity
- (custom metrics for autoscaling PVCs)
- (try to replicate current AWS setup, using different storage types for fast access and persistent storage)
- (mongo backups and/or storage snapshots)

### b) Sailing Analytics
Create a application server deployment that represents the current AWS configuration and has some of the features described in: [https://wiki.sapsailing.com/wiki/projects/cloud-orchestrator](https://wiki.sapsailing.com/wiki/projects/cloud-orchestrator)
- reproducable deployments
- Smooth handling of node and pod failures (antiaffinity)
- horizontal pod autoscaler defined on custom metrics
- ALB ingress controller, adding rules for each deployment

### c) CustomResource
Define a custom resource and controller to eventually reach an Kubernetes operator that has the full capacity of the cloud orchestration requirements.

## 2. Gardener Cluster
Provisioned Gardener with AWS secret. Use region eu-west-3. You might extend the ```shoot.yaml``` in the editor within the dashboard using [9] as reference. E.g. the clusterautoscaler. 

### Create a storage class that allows volume expansion
We need to create a storage class to allow volume expansion, in case we want to upgrade our mongo diskspace. Therefore created ```gp2-allow-volume-expansion-storageclass.yaml```, which is a copy of the Gardener gp2 storageclass with an additional property ```allowVolumeExpansion: true```. See also[10].

### Hibernation
Don't forget to hibernate the cluster if you don't need anymore. This will save costs on the AWS bill.

## 3. Helm
A short update on helm, after the confusion about Tiller and Helm Classic Tiller has been removed in Helm 3. See[5]

## 4. Docker registry secret
To pull an image from a private repository you need a secret for that[6]: 
```
kubectl create secret docker-registry <secret-name> --docker-server=https://index.docker.io/v1/ --docker-username=<your-name> --docker-password=<your-pword> --docker-email=<your-email>   
```
You might use the createDockerRegistrySecret script provided within /docker.

After that you may use that secret as follows[7]

## 5. Mongodb-replicaset

### Install the replicaset in the Kubernetes cluster
I use the helm chart provided in [4]. So first add the repository with:
```
helm repo add stable https://kubernetes-charts.storage.googleapis.com/
```
Then download and unpack the chart:
```
helm pull stable/mongodb-replicaset --untar 
```
Now create a copy of the ``values.yaml`` and give it a meaningfull name (``mongo.yaml``). Modify that file according to your needs. 
After that install your chart with:
```
helm install -f mongo.yaml <helm-release-name> ./mongodb-replicaset
```
This creates a helm release with your provided values. The list of possible values is described here[8].

A ```mongo.yaml``` is checked out in our git, containing a couple of extra annotations, e.g. a storageClass provided by AWS.


### Test
To test the mongo connection spin up a debug container and install mongo. Use the client-service as connection host. 
```
kubectl run -i --tty --rm debug0 --image=ubuntu --restart=Never -- bash -c "apt-get update && apt-get install mongodb-clients -y && bash"
```
```
mongo "mongodb://mongo-live-rs-client.default.svc.cluster.local/?replicaSet=<replicaSetName-from-mongo.yaml>"
```
Optionally you can just use the service name defined in the mongo.yaml:
```
mongo "mongodb://<name-in-mongo.yaml>-client/?replicaSet=<replicaSetName-from-mongo.yaml>
```


Now you can look for the primary executing ``db.isMaster()`` in the mongo shell. Delete that pod with ``kubectl delete pod <name>``, and watch for the mongo shell to reconnect to the newly elected primary.

## Sailing Analytics

### Pod and Service
Created a basic pod specification, see ```sailing-test-master-pod.yaml```. Also a ```service-test-master.yaml```. Applied to cluster, everything went fine. Connected via a ```kubectl port-forward pod/sailing-test-master 8081:8888```.

Attempted a master data import from a big event, during which the following was experienced:
- Mongo ran out of storage. Adjusted the pvc, after that the mongo pods came back without need to touch them -> it should be possible to automate this
- Several times a mongo pod has been killed by the kubelet, as the node went out of memory. The sailing pod switches to another mongo pod, nothing to do manually. After this has happened a couple of times the MDI goes into a halt, but resumes after some time.
-> Get a better understanding of the Gardener cluster autoscaler, imo this should have triggered a new node


## RabbitMQ

In order to be able to create a Sailing Analytics master-replica set we need a rabbitMQ in our cluster.
Get the helm chart with
```
helm pull bitnami/rabbitmq --untar
```
and adjust the ```values.yaml``` accordingly. There is a ```rabbitmq.yaml``` in ```/kubernetes``` containing a couple of modifications. If you do not want to write a password specifically in there create a Kubernetes secret, using:
```
k create secret generic <secret-name> --from-literal=rabbitmq-password='<password>'
```
Be carefull to match rabbitmq-password as key, as this is defined within the helm chart of bitnami/rabbitmq (see l259 in /helm-charts/rabbitmq/templates/statefulset.yaml). After that add the secret name in the rabbitmq.yaml, l75.

## Sailing Analytics

There is a helm chart for the sailing analytics in ```/helm-charts/sap-sailing-analytics``` along with some values in ```values.yaml```. 

### Sailing Analytics Operator
Using operator-sdk[11] there is a operator deployed into the garderner cluster. The CRD is within /sapsailing-operator/deploy/crds. To use create a resource yaml and apply it with kubectl apply -f. This will spin up a master-replica set, together with the according services. You may change some values of an already deployed resource, e.g. the number of replicas.
Configured within the operator is an antiaffinity for the master and replica pods.

## Monitoring
There is a prometheus and a grafana deployed in the namespace "monitoring". They have been deployed by using the helm-charts provided under the helm-charts folder. The values have been modified. However there is still some configuration work to do.

To access grafana run ```helm status -n monitoring grafana```, read the instructions on how to connect. To connect prometheus as datasource to grafana run helm status for the prometheus release and add the service DNS name to grafana as datasource, format for the datasource is: ```url:port```.

The rabbitMQ and the mongoDB deployments are already connected to prometheus. Still work in progress is the node metrics, as gardener collects them in a special way.

## Ingress / AWS ALB ingress controller
Zalando has made an effort to open-source an AWS ALB ingress controller that is capable of changing listener rules on a given ALB, see[12]. This looks like a better fit to our needs, compared with the standard AWS ALB ingress controller that deploys an ALB for each ingress resource, see[13].

To be able to isolate access to AWS in different containers you need to solve the problem that AWS adds IAM profiles to whole nodes, instead of containers, see context here[14]. Therefore kube2iam could be a solution.

To deploy the current state of the kube-ingress controller apply ```ingress-serviceaccount.yaml``` and ```aws-alb-ingress-controller.yaml```. Before that you will need a IAM role that you provide to the controller, the role needs the policy specified in ```aws-alb-ingress-controller-policy.json```. Also you will need to add an access key and secret as ENV variables to the pods created by the container.

### Tips:
- [install kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/) 

- [install helm](https://helm.sh/docs/intro/install/)

Add alias and bash completion for the above mentioned, append to .bashrc:
```bash
# kubectl bash comletion
source <(kubectl completion bash)
# kubectl bash alias
alias k=kubectl
complete -F __start_kubectl k
# helm bash completion
source <(helm completion bash)
# helm alias
alias h=helm
complete -F __start_helm h
```


### Resources:

[1] https://cloud.google.com/blog/products/containers-kubernetes/best-practices-for-building-kubernetes-operators-and-stateful-apps    
[2] https://book.kubebuilder.io/quick-start.html    
[3] https://www.mongodb.com/blog/post/introducing-the-mongodb-enterprise-operator-for-kubernetes     
[4] https://github.com/helm/charts/tree/master/stable/mongodb-replicaset    
[5] https://helm.sh/docs/faq/#removal-of-tiller    
[6] https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/#create-a-secret-by-providing-credentials-on-the-command-line    
[7] https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/#create-a-pod-that-uses-your-secret    
[8] https://github.com/helm/charts/tree/master/stable/mongodb-replicaset#configuration    
[9] https://github.com/gardener/gardener/blob/master/example/90-shoot.yaml    
[10] https://kubernetes.io/docs/concepts/storage/storage-classes/#the-storageclass-resource
[11] https://sdk.operatorframework.io/docs/helm/quickstart/
[12] https://github.com/zalando-incubator/kube-ingress-aws-controller
[13] https://github.com/kubernetes-sigs/aws-alb-ingress-controller


[1]: https://cloud.google.com/blog/products/containers-kubernetes/best-practices-for-building-kubernetes-operators-and-stateful-apps
[2]: https://book.kubebuilder.io/quick-start.html
[3]: https://www.mongodb.com/blog/post/introducing-the-mongodb-enterprise-operator-for-kubernetes
[4]: https://github.com/helm/charts/tree/master/stable/mongodb-replicaset
[5]: https://helm.sh/docs/faq/#removal-of-tiller
[6]: https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/#create-a-secret-by-providing-credentials-on-the-command-line
[7]: https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/#create-a-pod-that-uses-your-secret
[8]: https://github.com/helm/charts/tree/master/stable/mongodb-replicaset#configuration
[9]: https://github.com/gardener/gardener/blob/master/example/90-shoot.yaml
[10]: https://kubernetes.io/docs/concepts/storage/storage-classes/#the-storageclass-resource
[11]: https://sdk.operatorframework.io/docs/helm/quickstart/
[12]: https://github.com/zalando-incubator/kube-ingress-aws-controller
[13]: https://github.com/kubernetes-sigs/aws-alb-ingress-controller
