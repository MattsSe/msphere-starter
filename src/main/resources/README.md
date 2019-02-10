# {projectname}

## Installation

### Download the official Mindsphere® java sdk using one of the following options:
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
  
