# SAP Sailing Analytics on Kubernetes
## First objectives
Create a cluster with a running Sailing Analytics Master Replica Set and all needed central services: rabbitMq, MongoDB

After that think of a CustomResourceDefinition to model an event accordingly and implement a controller that handles those CRDs accordingly by creating deployments.

To create a controller we can use kubebuilder[2].


## Gardener Cluster
Provisioned Gardener with AWS secret. Use region eu-west-3. You might extend the ```shoot.yaml``` in the editor within the dashboard using [9] as reference. E.g. the clusterautoscaler. 

### Create a storage class that allows volume expansion
We need to create a storage class to allow volume expansion, in case we want to upgrade our mongo diskspace. Therefore created ```gp2-allow-volume-expansion-storageclass.yaml```, which is a copy of the Gardener gp2 storageclass with an additional property ```allowVolumeExpansion: true```. See also[10].

## Helm
A short update on helm, after the confusion about Tiller and Helm Classic Tiller has been removed in Helm 3.See[5]

## Docker registry secret
To pull an image from a private repository you need a secret for that: 
>kubectl create secret docker-registry <secret-name> --docker-server=https://index.docker.io/v1/ --docker-username=<your-name> --docker-password=<your-pword> --docker-email=<your-email>
[6]. You might use the createDockerRegistrySecret script provided within /docker.

After that you may use that secret as follows[7]

## Mongodb-replicaset

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
Now you can look for the primary executing ``db.isMaster()`` in the mongo shell. Delete that pod with ``kubectl delete pod <name>``, and watch for the mongo shell to reconnect to the newly elected primary.

## Sailing Analytics


### Tips:
[install kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/) 

[install helm](https://helm.sh/docs/intro/install/)

Add alias and bash completion for the above mentioned, append to .bashrc:
```
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
[1][https://cloud.google.com/blog/products/containers-kubernetes/best-practices-for-building-kubernetes-operators-and-stateful-apps]
[2][https://book.kubebuilder.io/quick-start.html]
[3][https://www.mongodb.com/blog/post/introducing-the-mongodb-enterprise-operator-for-kubernetes]
[4][https://github.com/helm/charts/tree/master/stable/mongodb-replicaset]
[5][https://helm.sh/docs/faq/#removal-of-tiller]
[6][https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/#create-a-secret-by-providing-credentials-on-the-command-line]
[7][https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/#create-a-pod-that-uses-your-secret]
[8][https://github.com/helm/charts/tree/master/stable/mongodb-replicaset#configuration]
[9][https://github.com/gardener/gardener/blob/master/example/90-shoot.yaml]
[10][https://kubernetes.io/docs/concepts/storage/storage-classes/#the-storageclass-resource]