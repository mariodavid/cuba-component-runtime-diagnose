-- begin DDCRD_DIAGNOSE_EXECUTION_LOG
create table DDCRD_DIAGNOSE_EXECUTION_LOG (
    ID varchar(32),
    VERSION integer not null,
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    --
    EXECUTION_SUCCESSFUL boolean not null,
    EXECUTION_TIMESTAMP datetime(3) not null,
    EXECUTION_USER varchar(255),
    EXECUTION_RESULT_FILE_ID varchar(32),
    DIAGNOSE_TYPE varchar(255) not null,
    EXECUTION_TYPE varchar(255) not null,
    --
    primary key (ID)
)^
-- end DDCRD_DIAGNOSE_EXECUTION_LOG
