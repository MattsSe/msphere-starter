# Mindsphere starter
A bare starter template for the Mindsphere® java sdk with gradle


## Installation


### Use the npm package

```bash
# install package
npm install create-msphere -g

# create new starter project
create-msphere <ARGS>
```

### Use the repo

```bash
git clone https://github.com/MattsSe/msphere-starter
cd msphere-starter
gradlew build

```


## Usage

Create a new project from the commandline
Both options create a new project using gradle. 

### 1. create a new starter project

#### With the installed npm package

```bash
create-msphere
```

#### With the build repo

```bash
java -jar build/libs/create-msphere-0.1.0.jar <ARGS>
```

### 2. Download the official Mindsphere® java sdk using one of the following options:
#### *Option 1: Download directly with gradle*
Simply run

```bash
./gradlew getSDK
```
If this fails then the download url is no longer valid. Please use *Option 2* and file an issue.


#### *Option 2: Manually via browser*
1. Download the MindSphere SDK from the [Siemens Industry Online Support Portal](https://support.industry.siemens.com/cs/ww/de/view/109757603). Requires a Siemens account. 
Download the zip file and put it into the [./libs](./libs) folder. You don't need to unzip it.
2. Install the sdk manually:
```bash
./gradlew installSDK
```

#### Build project
```bash
./gradlew build
```

