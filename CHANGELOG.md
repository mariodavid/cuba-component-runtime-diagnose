# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).


## [1.4.0] - 2019-09-10

### Added
- Select DataStore for SQL / JPQL console in order to execute SQL / JPQL statements against different then the MAIN data store
- Diagnose Wizard now exports the SQL / JPQL result set in proper CSV format
- auto import statements for entities for groovy scripts through configuration (#1)
- auto import statements for arbitrary classes for groovy script through configuration (#1)

### Dependencies
- CUBA 7.1.x
- jsqlparser 2.1

## [1.3.0] - 2019-03-30

### Dependencies
- CUBA 7.0.x

## [1.2.0] - 2018-10-30

### Bugfix
- handle no-results in SQL execution correctly (#23)

### Dependencies
- CUBA 6.10.x

## [1.1.0] - 2018-07-19

### Added

- handle tab key in groovy & SQL console
- GroovyScriptBindingSupplier mechanism to add binding variables for groovy scripts
- added following shorthand methods in groovy scripts:
  - `bean(String springBeanName)` gives you a arbitrary bean of the Spring context
  - `getSql()` returns a instance of `groovy.sql.SQL` for the main datasource
  - `getSql('myDatasourceName')` returns a instance of `groovy.sql.SQL` for the given additional datasource
      

### Dependencies
- CUBA 6.9.x


## [1.0.0] - 2018-04-11

This is a marker release. It is meant to show the the majority of the APIs according to the Semver conventions.

### Dependencies
- CUBA 6.8.x

## [0.4.1] - 2017-12-19

### Added
- possibility to convert JPQL to SQL in the JPQL console

## [0.4.0] - 2017-11-29

### Added
- support for comments in JPQL console

### Dependencies
- CUBA 6.7.x

## [0.3.1] - 2017-11-26


## [0.3.0] - 2017-11-25

### Added
- JPQL support

## [0.2.0] - 2017-09-22

### Added
- loads dynamic metadata information from BuildInfo (CUBA 6.6 feature) [#6][#9]
- support for audit log of runtime diagnose execution [#10]
- database support for HSQLDB, MySQL, PostgreSQL and Oracle

### Dependencies
- CUBA 6.6.x

## [0.1.0] - 2017-06-28

### Added
- interactive groovy console
- interactive SQL console
- diagnose wizard for non-interactive support cases


### Dependencies
- CUBA 6.5.x
