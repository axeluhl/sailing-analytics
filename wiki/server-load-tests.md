# Server Load Tests

It can make sense to put a server under heavy load to test how it reacts to such increased request numbers. One way to do that is to fire up instances that start a browser that points to an URL. There is an AMI that helps you doing this. Execute the following steps.

- Select the AMI `Browser Test 1.0`
- Make sure to select `m1.medium` instances. You can not use other instances as the number is limited. Amazon has only granted us to start more than 50 instances when they are of that type.
- Input the URL you want the browser to load into the User Data field. No quotes or such needed.
- Start up to `900` instances

Currently we can not use Spot instances as this is a separate limit pool. 