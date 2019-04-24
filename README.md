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


##### Running

To run the webserver run in terminal:

```
bash run_server.sh <path_to_AWS_SDK_directory>
```

For the loadbalancer:

```
bash run_loadbalancer.sh <path_to_AWS_SDK_directory>
```

For the mss:

```
bash run_mss_server.sh <path_to_AWS_SDK_directory>
```

### BitTool

Currently the main under-development bittool is `BitTool.java` present in the root directory of the project. 
The other bit tool `DynamicTool.java` is incomplete and for testing purposes only.