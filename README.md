# CNV Project - Group 1

### Report

https://docs.google.com/document/d/10gtuVZq2Gyty4lxQBokVya3Mb3Le7LiKvWXuKjCphgk/edit

### Instructions

##### Requirements

- Java 7
- Amazon AWS SDK

##### Compiling

Run in terminal:

```
bash compile.sh <path_to_AWS_SDK_directory>
```

Note that the sdk directory should have the folders `aws-java-sdk-<version>/lib`
and `aws-java-sdk-<version>/third-party/lib` as provided by the downloaded and unzipped sdk from amazon.
This path must not contain an ending '/' character.

For example:
`bash compile.sh ../aws-java-sdk-1.11.534`


##### Running

First run the MSS: {DEPRECATED 2}

```
bash run_mss_server.sh <path_to_AWS_SDK_directory>
```

Second, run one or more web servers, each in a separate ec2 instance by running the following command
on each one:

```
bash run_server.sh <path_to_AWS_SDK_directory>
```

Third, run the loadbalancer like so:

```
bash run_loadbalancer.sh <path_to_AWS_SDK_directory>
```


##### Testing locally

It is possible to test the system locally, by altering each run script to pass the command line argument `-localhost` 
to the java command.


### BitTool

Currently the main under-development bittool is `BitTool.java` present in the root directory of the project. 
The other bit tool `DynamicTool.java` is incomplete and for testing purposes only.
