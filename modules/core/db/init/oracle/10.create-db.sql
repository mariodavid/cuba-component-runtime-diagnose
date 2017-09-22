-- begin DDCRD_DIAGNOSE_EXECUTION_LOG
create table DDCRD_DIAGNOSE_EXECUTION_LOG (
    ID varchar2(32),
    VERSION number(10) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    --
    EXECUTION_SUCCESSFUL char(1) not null,
    EXECUTION_TIMESTAMP timestamp not null,
    EXECUTION_USER varchar2(255),
    EXECUTION_RESULT_FILE_ID varchar2(32),
    DIAGNOSE_TYPE varchar2(255) not null,
    EXECUTION_TYPE varchar2(255) not null,
    --
    primary key (ID)
)^
-- end DDCRD_DIAGNOSE_EXECUTION_LOG
