CREATE TABLE [users](
	[user_id] [bigint] NOT NULL,
	[username] [varchar] (50) NOT NULL,
	[password] [varchar] (50) NOT NULL,
	[name] [varchar] (20) NOT NULL,
	[surname] [varchar] (20) NOT NULL,
	[gender] [varchar] (10) NOT NULL,
	[status] [varchar] (10) NOT NULL,
	[expiry] [datetime2] (6) NOT NULL,
	[login_dt] [datetime2] (6) NULL,
	[creation_dt] [datetime2] (6) NOT NULL,
	[updated_dt] [datetime2] (6) NULL,
	[phone] [varchar] (20) NULL,
	[email] [varchar] (50) NULL,
	[address_one] [varchar] (50) NOT NULL,
	[address_two] [varchar] (50) NULL,
	[address_three] [varchar] (50) NULL,
	[city] [varchar] (20) NULL,
	[country] [varchar] (10) NULL,
	[zip_code] [varchar] (20) NULL,
	[role_id] [bigint] NOT NULL,
	[hash] [varchar] (128) NOT NULL,
	[salt] [varchar] (100) NULL,
	[logout_dt] [datetime2] (6) NULL,
	[created_by] [varchar] (50) NOT NULL,
	[modified_by] [varchar] (50) NULL,
    CONSTRAINT PK_users primary key(user_id))
GO

ALTER TABLE dbo.users
ADD password_changed_dt DATETIME2(6) NULL,

    expiry_15_days_email_sent BIT NOT NULL
        CONSTRAINT DF_users_expiry_15_days DEFAULT (0),

    expiry_7_days_email_sent BIT NOT NULL
        CONSTRAINT DF_users_expiry_7_days DEFAULT (0),

    expiry_email_sent BIT NOT NULL
        CONSTRAINT DF_users_expiry_email DEFAULT (0);
GO


UPDATE dbo.users
SET expiry = DATEADD(DAY, 90, SYSUTCDATETIME()),
    expiry_15_days_email_sent = 0,
    expiry_7_days_email_sent = 0,
    expiry_email_sent = 0
WHERE status = 'ACTIVE';
GO

-- Run in your application database

IF OBJECT_ID(N'dbo.Banners', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Banners
    (
        banner_id BIGINT IDENTITY(1,1)
            CONSTRAINT PK_Banners PRIMARY KEY,

        banner_type VARCHAR(5) NOT NULL,

        title NVARCHAR(200) NOT NULL,

        subtitle NVARCHAR(1000) NULL,

        alt_text NVARCHAR(250) NULL,

        background_color CHAR(7) NOT NULL,

        text_color CHAR(7) NOT NULL,

        active BIT NOT NULL
            CONSTRAINT DF_Banners_Active DEFAULT (1),

        starts_at DATETIME2(3) NULL,

        ends_at DATETIME2(3) NULL,

        image_content_type VARCHAR(10) NULL,

        image_bytes VARBINARY(MAX) NULL,

        -- Audit columns
        created_by BIGINT NOT NULL,

        created_at DATETIME2(3) NOT NULL
            CONSTRAINT DF_Banners_Created
            DEFAULT SYSUTCDATETIME(),

        updated_by BIGINT NULL,

        updated_at DATETIME2(3) NULL,

        -- Constraints
        CONSTRAINT CK_Banners_Type
            CHECK (banner_type IN ('TEXT', 'IMAGE')),

        CONSTRAINT CK_Banners_Schedule
            CHECK (
                starts_at IS NULL
                OR ends_at IS NULL
                OR ends_at > starts_at
            ),

        CONSTRAINT CK_Banners_Image
            CHECK (
                (
                    banner_type = 'TEXT'
                    AND image_content_type IS NULL
                    AND image_bytes IS NULL
                )
                OR
                (
                    banner_type = 'IMAGE'
                    AND image_content_type IS NOT NULL
                    AND image_content_type IN
                        ('image/png', 'image/jpeg')
                    AND image_bytes IS NOT NULL
                    AND DATALENGTH(image_bytes)
                        BETWEEN 1 AND 10485760
                )
            )
    );
END;
GO




IF OBJECT_ID(N'Folders', N'U') IS NULL
BEGIN

    CREATE TABLE Folders (

        folder_id BIGINT IDENTITY(1,1) PRIMARY KEY,

        parent_folder_id BIGINT NULL,

        folder_name NVARCHAR(255) NOT NULL,

        description NVARCHAR(1000) NULL,

        language_code VARCHAR(10) NOT NULL DEFAULT 'en',

        status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

        created_by NVARCHAR(255) NOT NULL,

        created_dt DATETIME2 NOT NULL
            DEFAULT SYSUTCDATETIME(),

        modified_by NVARCHAR(255) NULL,

        updated_dt DATETIME2 NULL,

        CONSTRAINT FK_Folders_Parent
            FOREIGN KEY (parent_folder_id)
            REFERENCES Folders(folder_id),

        CONSTRAINT CK_Folders_Status
            CHECK (status IN ('ACTIVE', 'DELETED'))
    );

    CREATE UNIQUE INDEX UX_Folders_Active_Name
    ON Folders(parent_folder_id, folder_name)
    WHERE status = 'ACTIVE';

END;
GO

IF OBJECT_ID(N'Files', N'U') IS NULL
BEGIN

    CREATE TABLE Files (

        file_id BIGINT IDENTITY(1,1) PRIMARY KEY,

        folder_id BIGINT NULL,

        file_name NVARCHAR(255) NOT NULL,

        file_path NVARCHAR(2000) NOT NULL,

        file_size BIGINT NOT NULL,

        content_type VARCHAR(255) NULL,

        status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

        created_by NVARCHAR(255) NOT NULL,

        created_dt DATETIME2 NOT NULL
            DEFAULT SYSUTCDATETIME(),

        modified_by NVARCHAR(255) NULL,

        updated_dt DATETIME2 NULL,

        CONSTRAINT FK_Files_Folders
            FOREIGN KEY (folder_id)
            REFERENCES Folders(folder_id),

        CONSTRAINT CK_Files_Status
            CHECK (status IN ('ACTIVE', 'DELETED')),

        CONSTRAINT CK_Files_Size
            CHECK (file_size >= 0)
    );

    CREATE INDEX IX_Files_Folder
    ON Files(folder_id, status);

END;
GO