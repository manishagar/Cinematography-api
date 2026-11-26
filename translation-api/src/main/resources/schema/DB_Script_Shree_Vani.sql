USE CinematographyDB;
GO

IF OBJECT_ID(N'dbo.Language_Translations', N'U') IS NULL
BEGIN

    CREATE TABLE dbo.Language_Translations
    (
        translation_id BIGINT IDENTITY(1,1)
            CONSTRAINT PK_Language_Translations
            PRIMARY KEY,

        original_text NVARCHAR(MAX) NOT NULL,

        source_hash VARCHAR(64) NOT NULL,

        source_language VARCHAR(10) NOT NULL,

        target_language VARCHAR(10) NOT NULL,

        translated_text NVARCHAR(MAX) NOT NULL,

        translation_provider VARCHAR(30) NOT NULL
            CONSTRAINT DF_Translation_Provider
            DEFAULT 'GOOGLE',

        created_at DATETIME2(3) NOT NULL
            CONSTRAINT DF_Translation_Created
            DEFAULT SYSUTCDATETIME(),

        updated_at DATETIME2(3) NOT NULL
            CONSTRAINT DF_Translation_Updated
            DEFAULT SYSUTCDATETIME(),

        CONSTRAINT UQ_Language_Translations
            UNIQUE
            (
                source_hash,
                source_language,
                target_language
            )
    );

END;
GO