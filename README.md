[ ![Download](https://api.bintray.com/packages/mariodavid/cuba-components/cuba-component-runtime-diagnose/images/download.svg) ](https://bintray.com/mariodavid/cuba-components/cuba-component-runtime-diagnose/_latestVersion)
[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/mariodavid/cuba-component-runtime-diagnose.svg?branch=master)](https://travis-ci.org/mariodavid/cuba-component-runtime-diagnose)
[![Coverage Status](https://coveralls.io/repos/github/mariodavid/cuba-component-runtime-diagnose/badge.svg)](https://coveralls.io/github/mariodavid/cuba-component-runtime-diagnose)

# CUBA Platform Component - Runtime diagnose

This application component can be used for interactive runtime application diagnose and debugging of a [CUBA](https://www.cuba-platform.com/) application.
It is based on the idea of the [Grails console](http://plugins.grails.org/plugin/console).

It mainly consists of the three parts:

* interactive Groovy console
* interactive JPQL / SQL console
* non-interactive diagnose wizard


## Installation


1. `runtime-diagnose` is available in the [CUBA marketplace](https://www.cuba-platform.com/marketplace)
2. Select a version of the add-on which is compatible with the platform version used in your project:

| Platform Version | Add-on Version |
| ---------------- | -------------- |
| 6.9.x            | 1.1.x          |
| 6.8.x            | 1.0.x          |
| 6.7.x            | 0.4.x          |
| 6.6.x            | 0.2.x - 0.3.x  |
| 6.5.x            | 0.1.x          |


The latest version is: [ ![Download](https://api.bintray.com/packages/mariodavid/cuba-components/cuba-component-runtime-diagnose/images/download.svg) ](https://bintray.com/mariodavid/cuba-components/cuba-component-runtime-diagnose/_latestVersion)

Add custom application component to your project:

* Artifact group: `de.diedavids.cuba.runtimediagnose`
* Artifact name: `runtime-diagnose-global`
* Version: *add-on version*

```groovy
dependencies {
  appComponent("de.diedavids.cuba.runtimediagnose:runtime-diagnose-global:*addon-version*")
}
```

## Groovy console
The groovy console allows you to interactively inspect the running application. You enter a groovy script and execute it in an ad-hoc fashion.

![Screenshot Groovy-Console](https://github.com/mariodavid/cuba-component-runtime-diagnose/blob/master/img/groovy-console-screenshot.png)

> WARN: Using the groovy console in production can be dangerous. Make sure that you will use the security subsystem properly so that only allowed users are able to execute code in production. For more information see the section about security

The console uses the [Scripting](https://doc.cuba-platform.com/manual-6.4/scripting.html) Interface of the platform in order to execute groovy code. Therefore most of the features of the scripting interface apply to the groovy console as well.

### Using existing classes

In order to use existing platform beans or your application specific beans, you can use `bean(TimeSource)` to get a reference to abitrary Spring beans.

> NOTE: To reference classes by name you have to manually add the corresponding import statements at the top of the scripts

If you want to define custom variables that are accessible in your scripts, you create a Spring bean which implements `GroovyScriptBindingSupplier`.

    @Component('myapp_MyAdditionalBindingSupplier')
    class MyAdditionalBindingSupplier implements GroovyScriptBindingSupplier {
    
        @Inject
        TimeSource timeSource
        
        @Override
        Map<String, Object> getBinding() {
            [ timeSource: timeSource ]
        }
    }
    
You can define multiple Spring beans that implement `GroovyScriptBindingSupplier` in your project. 
All Maps will be merged and be accessible in the groovy script.

### Execution results

There are different results of a groovy script (displayed in the different tabs). The actual result of the script (meaning the return value of the last statement) is displayed in the first tab. The stacktrace tab displayes the stacktrace of a possible exception that occurs during script execution. The tab executed script shows the actual executed script.

#### Result log

Since `STDOUT` and `STDERR` will not captured through the runtime, `println 'hello world'` will not be included in any of the execution results. To make debug statements you can use the `log` variable which is passed into the script.

The possible methods are:

* `log.debug('debug information')`
* `log.warn('warnnings')`
* `log.error('error information')`

### Download of the execution results

Execution results can be downloaded through the corresponding button. It will create a zip file which will contain the different execution results in different files. Additionally, there is a file called `environmentInformation.log` which will include information about the current environment of execution (like information about the user that executed the script, information about the application itself etc.)

## JPQL / SQL console
The JPQL / SQL console allows you to interactivly interact with the database using raw JPQL / SQL statements. You enter a JPQL / SQL script and execute it in an ad-hoc fashion.

![Screenshot SQL-Console](https://github.com/mariodavid/cuba-component-runtime-diagnose/blob/master/img/sql-console-screenshot.png)


> NOTE: for normal data diagnosis the [Entity inspector](https://doc.cuba-platform.com/manual-6.4/entity_inspector.html) is oftentimes more user friendly, even for debugging purposes. 
Usage of the SQL-console is to be preferable to the entity inspector if you want to access data across tables using joins for example.
The JPQL console is useful if you want to test your JPQL queries that you want to use in your application e.g. 


Results of a JPQL / SQL statement are displayed in a table in the result tab. The result can be downloaded using the Excel button in the Results tab.

The JPQL / SQL console supports comments in the form of `-- single line comment` and `/* multi line comments */`.

In the JPQL console you additionally have the possibility to convert your written JPQL into an equivivalent SQL query. 
This can be handy sometimes when working with the default mechanism of CUBA to load in data via SQL files e.g. 

### Security of the SQL-Console
By default, only SELECT stements are allowed to execute through the SQL-Console. 
If you want to execute other types of SQL statements like `INSERT` or `ALTER` it has to be explicitly configured the application component through CUBAs App properties UI: `Administration > Application properties > console`

The following configuration options allow different statement types:
* `runtime-diagnose.sql.allowDataManipulation`
  * `INSERT INTO...`
  * `UPDATE ...`
  * `DELETE ...`
  * `MERGE ...`
  * `REPLACE ...`
  * `TRUNCATE ...`
* `runtime-diagnose.sql.allowSchemaManipulation`
  * `DROP ...`
  * `CREATE TABLE ...`
  * `CREATE VIEW ...`
  * `ALTER ...`
  * `ALTER VIEW ...`
  * `CREATE INDEX ...`
* `runtime-diagnose.sql.allowExecuteOperations`
  * `EXECUTE ...`
  * `SET ...`

## Diagnose wizard

The last part is the diagnose wizard. This option is relevant if you as a developer or customer support person don't have direct access to the running application, because of security reasons or it is boxed software that is running out of your control. You could send your counterpart on customer side a text file which the the user should execute in the Groovy / SQL console, but this process is fairly insecure as well as error prone.

In these cases you can send the person a zip file (as a black box) and tell them to upload this file in the diagnose wizard. The person will be guided through the different steps, executed the scripts and gets back the execution results (as a zip file) that should be handed back to you.

![Screenshot Diagnose Wizard](https://github.com/mariodavid/cuba-component-runtime-diagnose/blob/master/img/diagnose-wizard-screenshot.png)

### Checks on the diagnose file
There are some checks in place in the wizard that will ensure the correctness of the zip file. Within the zip archive, there has to be the following files:
* diagnose.groovy / diagnose.sql
* manifest.json

The diagnose.(sql|groovy) contains the executable script. The manifest file describes some metadata on the diagnose archive. Here's a valid manifest.json file:

    {
      "appVersion": "1.1",
      "appName": "runtime-diagnose-app",
      "producer": "Company Inc.",
      "diagnoseType": "GROOVY"
    }

The `diagnoseType` has to be either `GROOVY` or `SQL`. 

If the values in the manifest file do not match the expected values from the application, the script cannot be executed.

The diagnose files can be created manually or preferably from the Groovy / SQL Console. After defining the diagnose script, the "Download Diagnose File" lets the developer create the diagnose file that can be handed to the customer representative. 


## Audit Log of diagnose execution

Since running these kind of operations can be dangerous sometimes, there is the possibility to write an audit log for the execution
of those diagnosis. The audit log is written for ad-hoc diagnosis as well as diagnose wizard executions.

The audit logging can be enabled through CUBAs App properties UI: `Administration > Application properties`

* `runtime-diagnose.log.enabled` - enables / disables the audit logging feature for diagnose executions
* `runtime-diagnose.log.logDiagnoseDetails` - enables / disables detailed logging, including the content of the diagnose script, diagnose result


