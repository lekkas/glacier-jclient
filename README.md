Glacier-JClient
===============
This application is a command-line interface to [Amazon Glacier](http://aws.amazon.com/glacier)
written in Java. It supports resume functionality for uploads and also keeps a local cache
of vault metadata and their inventories.

Commands
--------
```
usage: glacier-jclient [-h] [--region <region>] [--credentials <file>] COMMAND ...

Command line interface for Amazon Glacier

optional arguments:
  -h, --help             show this help message and exit
  --region <region>      Set region [us-east-1, us-west-1, us-west-2, eu-west-1, ap-southeast-2, ap-northeast-1]
  --credentials <file>   Location of AWS credentials

commands:
  COMMAND                Description
    vault (v)            Vault operations
    archive (a)          Archive operations
    job (j)              Job operations
    cache (c)            Display cache
```

For more help on each command, you can run <code>glacier-jclient [COMMAND] -h</code>.

The key operations currently supported by the application are described in the following table.

| Operation | Command |
|----------:|---------|
|View local cache|<code>glacier-jclient vault --list</code>|
|Upload archive|<code>glacier-jclient archive --vault <em>vaultName</em> --upload <em>fileName</em></code>|
|Abort upload|<code>glacier-jclient archive --vault <em>vaultName</em> --abort <em>uploadId</em></code>|
|List all vaults|<code>glacier-jclient vault --list</code>|
|Create vault|<code>glacier-jclient vault --create <em>vaultName</em></code>|
|Delete vault|<code>glacier-jclient vault --delete <em>vaultName</em></code>|
|Retrieve vault metadata|<code>glacier-jclient vault --meta <em>vaultName</em></code>|
|Retrieve vault inventory|<code>glacier-jclient vault --inventory <em>vaultName</em></code>|
|List vault jobs|<code>glacier-jclient job --list <em>selection</em></code>|
|Display local cache|<code>galcier-jclient cache|

Configuration
-------------
By default, glacier-jclient will search for your Amazon credentials in *~/.aws/credentials*. Alternatively you can set the location of your credentials file by using the --credentials argument. This file should contain your credentials in the following format:
```
aws_access_key_id=<YourAccessKey>
aws_access_secret_key=<YourSecretKey>
```

TODO
----
* Download archives without passing a user-defined cost ($) threshold.
* Option to change log file location.
* Option to keep the application waiting for the invetory retrieval job to be completed (--wait)

Contact
-------
* For bugs or feature requests please create a [glacier-jclient github
  issue](https://github.com/lekkas/glacier-jclient/issues).



