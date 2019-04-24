# cnv-project

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
and `aws-java-sdk-<version>/third-party` as provided by the downloaded and unzipped sdk from amazon.
This path must not contain an ending '/' character.


##### Running

To run the webserver run in terminal:

```
bash run_server.sh <path_to_AWS_SDK_directory>
```

And for the loadbalancer:

```
bash run_loadbalancer.sh <path_to_AWS_SDK_directory>
```


