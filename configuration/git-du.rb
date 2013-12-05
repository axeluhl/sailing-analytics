#!/usr/bin/env ruby -w
require 'set'

display_branches = ARGV

if display_branches.empty?
    puts "You need to specify a branch or ALL for all branches"
end

packed_blobs = {}

class PackedBlob
    attr_accessor :sha, :type, :size, :packed_size, :offset, :depth, :base_sha, :is_shared, :branch
    def initialize(sha, type, size, packed_size, offset, depth, base_sha)
        @sha = sha
        @type = type
        @size = size
        @packed_size = packed_size
        @offset = offset
        @depth = depth
        @base_sha = base_sha
        @is_shared = false
        @branch = nil
    end
end

class Branch
    attr_accessor :name, :blobs, :non_shared_size, :non_shared_packed_size, :shared_size, :shared_packed_size, :non_shared_dependable_size, :non_shared_dependable_packed_size
    def initialize(name)
        @name = name
        @blobs = Set.new
        @non_shared_size = 0
        @non_shared_packed_size = 0
        @shared_size = 0
        @shared_packed_size = 0
        @non_shared_dependable_size = 0
        @non_shared_dependable_packed_size = 0
    end
end

dependable_blob_shas = Set.new

if not File.exists?('packed_blobs.bin')

    # Collect every packed blobs information
    for pack_idx in Dir[".git/objects/pack/pack-*.idx"]
        puts pack_idx
        pack_counter = 0
        IO.popen("git verify-pack -v #{pack_idx}", 'r') do |pack_list|
            pack_list.each_line do |pack_line|
                pack_counter += 1
                pack_line.chomp!
                if not pack_line.include? "delta"
                    $stdout.write "\r #{pack_counter} packs"; $stdout.flush;
                    sha, type, size, packed_size, offset, depth, base_sha = pack_line.split(/\s+/, 7)
                    size = size.to_i
                    packed_size = packed_size.to_i
                    packed_blobs[sha] = PackedBlob.new(sha, type, size, packed_size, offset, depth, base_sha)
                    dependable_blob_shas.add(base_sha) if base_sha != nil
                else
                    break
                end
            end
        end
        puts " "
    end

    puts "Writing packed blobs..."
    File.open('packed_blobs.bin','wb') do |f|
          f.write Marshal.dump(packed_blobs)
    end

    puts "Writing dependable blob SHAs..."
    File.open('dependable_blob_shas.bin','wb') do |f|
          f.write Marshal.dump(dependable_blob_shas)
    end

else
    puts "Reading packed blobs from file! This means that information won't be up 2 date but process is faster!"
    packed_blobs = Marshal.load(File.open('packed_blobs.bin', 'rb'))
    dependable_blob_shas = Marshal.load(File.open('dependable_blob_shas.bin', 'rb'))
    puts "Read #{packed_blobs.size()} packed blobs and #{dependable_blob_shas.size()} SHAs..."
end

branches = {}

# Now check all blobs for every branches in order to determine whether it's shared between branches or not
IO.popen("git branch --list", 'r') do |branch_list|
    branch_list.each_line do |branch_line|
        # For each branch
        branch_name = branch_line[2..-1].chomp
        if display_branches.include?(branch_name) or display_branches.include?("ALL")
            puts "* #{branch_name}"
            if not File.exists?("#{branch_name}.bin")
                puts "    file #{branch_name}.bin does not exist - reading data from repository"
                branch = Branch.new(branch_name)
                branches[branch_name] = branch
                counter = 0; commit_counter = 0
                IO.popen("git rev-list #{branch_name}", 'r') do |rev_list|
                    all_commit_lines = rev_list.readlines
                    all_commit_lines.each do |commit|
                        commit_all_count = all_commit_lines.count
                        commit_counter += 1
                        # Look into each commit in order to collect all the blobs used
                        blobs_all = `git ls-tree -zrl #{commit}`.split("\0")
                        for object in blobs_all
                            counter += 1
                            bits, type, sha, size, path = object.split(/\s+/, 5)
                            if type == 'blob'
                                blob = packed_blobs[sha]
                                if blob != nil
                                    if counter % 5 == 0
                                      $stdout.write "\r    #{counter} blobs #{commit_counter}/#{commit_all_count} commits"; $stdout.flush;
                                    end
                                    branch.blobs.add(blob)
                                    if not blob.is_shared
                                        if blob.branch != nil and blob.branch != branch
                                            # this blob has been used in another branch, let's set it to "shared"
                                            blob.is_shared = true
                                            blob.branch = nil
                                        else
                                            blob.branch = branch
                                        end
                                    end
                                end
                            else
                                print "F"
                            end
                        end
                    end
                end
                print "\n    Writing branch data to file..."
                File.open("#{branch_name}.bin",'wb') do |f|
                      f.write Marshal.dump(branch)
                end
                puts "OK"
            else
                print "\n    Loading branch data from file #{branch_name}. Remove this file to get new data..."
                branches[branch_name] = Marshal.load(File.open("#{branch_name}.bin", 'rb'))
                puts "OK"
            end
        end
    end
end

# Now iterate on each branch to compute the space usage for each
branches.each_value do |branch|
    branch.blobs.each do |blob|
        if blob.is_shared
            branch.shared_size += blob.size
            branch.shared_packed_size += blob.packed_size
        else
            if dependable_blob_shas.include?(blob.sha)
                branch.non_shared_dependable_size += blob.size
                branch.non_shared_dependable_packed_size += blob.packed_size
            else
                branch.non_shared_size += blob.size
                branch.non_shared_packed_size += blob.packed_size
            end
        end
    end
    # Now print it if wanted
    if display_branches.empty? or display_branches.include?(branch.name) or display_branches.include?("ALL")
        puts "branch: %s" % branch.name
        puts "\tnon shared:"
        puts "\t\tpacked: %s kb" % (branch.non_shared_packed_size / 1000)
        puts "\t\tnon packed: %s kb" % (branch.non_shared_size / 1000)
        puts "\tnon shared but with dependencies on it:"
        puts "\t\tpacked: %s kb" % (branch.non_shared_dependable_packed_size / 1000)
        puts "\t\tnon packed: %s kb" % (branch.non_shared_dependable_size / 1000)
        puts "\tshared:"
        puts "\t\tpacked: %s kb" % (branch.shared_packed_size / 1000)
        puts "\t\tnon packed: %s kb" % (branch.shared_size / 1000), ""
    end
end
