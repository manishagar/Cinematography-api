CREATE TABLE dbo.Users
(
    user_id BIGINT IDENTITY(1,1) NOT NULL,

    first_name NVARCHAR(100) NOT NULL,
    last_name NVARCHAR(100) NULL,
    email NVARCHAR(255) NOT NULL,
    mobile_number NVARCHAR(20) NULL,

    password_hash NVARCHAR(500) NOT NULL,
    profile_image_path NVARCHAR(1000) NULL,

    email_verified BIT NOT NULL
        CONSTRAINT DF_Users_email_verified DEFAULT (0),

    mobile_verified BIT NOT NULL
        CONSTRAINT DF_Users_mobile_verified DEFAULT (0),

    account_status VARCHAR(20) NOT NULL
        CONSTRAINT DF_Users_account_status DEFAULT ('ACTIVE'),

    is_locked BIT NOT NULL
        CONSTRAINT DF_Users_is_locked DEFAULT (0),

    failed_login_attempts INT NOT NULL
        CONSTRAINT DF_Users_failed_login_attempts DEFAULT (0),

    last_login_at DATETIME2 NULL,
    password_changed_at DATETIME2 NULL,

    created_at DATETIME2 NOT NULL
        CONSTRAINT DF_Users_created_at DEFAULT (SYSUTCDATETIME()),

    created_by BIGINT NULL,
    updated_at DATETIME2 NULL,
    updated_by BIGINT NULL,

    is_deleted BIT NOT NULL
        CONSTRAINT DF_Users_is_deleted DEFAULT (0),

    CONSTRAINT PK_Users
        PRIMARY KEY (user_id),

    CONSTRAINT UK_Users_email
        UNIQUE (email),

    CONSTRAINT FK_Users_created_by
        FOREIGN KEY (created_by)
        REFERENCES dbo.Users(user_id),

    CONSTRAINT FK_Users_updated_by
        FOREIGN KEY (updated_by)
        REFERENCES dbo.Users(user_id)
);
GO

CREATE TABLE dbo.Roles
(
    role_id INT IDENTITY(1,1) NOT NULL,

    role_code VARCHAR(50) NOT NULL,

    role_name NVARCHAR(100) NOT NULL,

    description NVARCHAR(500) NULL,

    is_active BIT NOT NULL
        CONSTRAINT DF_Roles_IsActive DEFAULT (1),

    created_at DATETIME2 NOT NULL
        CONSTRAINT DF_Roles_CreatedAt DEFAULT (SYSUTCDATETIME()),

    CONSTRAINT PK_Roles
        PRIMARY KEY (role_id),

    CONSTRAINT UK_Roles_RoleCode
        UNIQUE (role_code)
);
GO

CREATE TABLE dbo.User_Roles
(
    user_role_id BIGINT IDENTITY(1,1) NOT NULL,

    user_id BIGINT NOT NULL,

    role_id INT NOT NULL,

    assigned_at DATETIME2 NOT NULL
        CONSTRAINT DF_UserRoles_AssignedAt DEFAULT (SYSUTCDATETIME()),

    assigned_by BIGINT NULL,

    is_active BIT NOT NULL
        CONSTRAINT DF_UserRoles_IsActive DEFAULT (1),

    CONSTRAINT PK_User_Roles
        PRIMARY KEY (user_role_id),

    CONSTRAINT FK_UserRoles_Users
        FOREIGN KEY (user_id)
        REFERENCES dbo.Users(user_id),

    CONSTRAINT FK_UserRoles_Roles
        FOREIGN KEY (role_id)
        REFERENCES dbo.Roles(role_id),

    CONSTRAINT FK_UserRoles_AssignedBy
        FOREIGN KEY (assigned_by)
        REFERENCES dbo.Users(user_id),

    CONSTRAINT UK_UserRoles_User_Role
        UNIQUE (user_id, role_id)
);
GO

CREATE TABLE dbo.Permissions
(
    permission_id INT IDENTITY(1,1) NOT NULL,

    permission_code VARCHAR(100) NOT NULL,

    permission_name NVARCHAR(150) NOT NULL,

    module_name VARCHAR(100) NOT NULL,

    description NVARCHAR(500) NULL,

    is_active BIT NOT NULL
        CONSTRAINT DF_Permissions_IsActive DEFAULT (1),

    CONSTRAINT PK_Permissions
        PRIMARY KEY (permission_id),

    CONSTRAINT UK_Permissions_PermissionCode
        UNIQUE (permission_code)
);
GO

CREATE TABLE dbo.Role_Permissions
(
    role_permission_id BIGINT IDENTITY(1,1) NOT NULL,

    role_id INT NOT NULL,

    permission_id INT NOT NULL,

    assigned_at DATETIME2 NOT NULL
        CONSTRAINT DF_RolePermissions_AssignedAt
        DEFAULT (SYSUTCDATETIME()),

    assigned_by BIGINT NULL,

    CONSTRAINT PK_Role_Permissions
        PRIMARY KEY (role_permission_id),

    CONSTRAINT FK_RolePermissions_Roles
        FOREIGN KEY (role_id)
        REFERENCES dbo.Roles(role_id),

    CONSTRAINT FK_RolePermissions_Permissions
        FOREIGN KEY (permission_id)
        REFERENCES dbo.Permissions(permission_id),

    CONSTRAINT FK_RolePermissions_AssignedBy
        FOREIGN KEY (assigned_by)
        REFERENCES dbo.Users(user_id),

    CONSTRAINT UK_RolePermissions_Role_Permission
        UNIQUE (role_id, permission_id)
);
GO

CREATE TABLE dbo.Refresh_Tokens
(
    refresh_token_id BIGINT IDENTITY(1,1) NOT NULL,

    user_id BIGINT NOT NULL,

    token_hash NVARCHAR(500) NOT NULL,

    device_name NVARCHAR(200) NULL,

    ip_address VARCHAR(45) NULL,

    expires_at DATETIME2 NOT NULL,

    revoked_at DATETIME2 NULL,

    created_at DATETIME2 NOT NULL
        CONSTRAINT DF_RefreshTokens_CreatedAt
        DEFAULT (SYSUTCDATETIME()),

    CONSTRAINT PK_Refresh_Tokens
        PRIMARY KEY (refresh_token_id),

    CONSTRAINT FK_RefreshTokens_Users
        FOREIGN KEY (user_id)
        REFERENCES dbo.Users(user_id)
);
GO

CREATE TABLE dbo.Password_Reset_Tokens
(
    reset_id BIGINT IDENTITY(1,1) NOT NULL,

    user_id BIGINT NOT NULL,

    token_hash NVARCHAR(500) NOT NULL,

    expires_at DATETIME2 NOT NULL,

    used_at DATETIME2 NULL,

    created_at DATETIME2 NOT NULL
        CONSTRAINT DF_Password_Reset_Tokens_created_at
        DEFAULT (SYSUTCDATETIME()),

    CONSTRAINT PK_Password_Reset_Tokens
        PRIMARY KEY (reset_id),

    CONSTRAINT FK_Password_Reset_Tokens_Users
        FOREIGN KEY (user_id)
        REFERENCES dbo.Users(user_id)
);
GO