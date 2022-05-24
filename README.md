# JPhonebookServer

JPhonebookServer is a reverse-engineered version of Fanvil's [Cloud Phonebook Server](https://www.fanvil.com/Uploads/Temp/download/20180920/5ba3831c562a8.pdf).

It was made due to the official app being a Windows-only desktop app, and me requiring a solution that can run on Linux servers.

## Using

To use, run `java -jar JPhonebookServer-{version}.jar` from a command line. You will need Java 16 or later.

Access the admin dashboard through `{ip}:8080/admin` to add directories and contacts. The default account has a username of `admin` and a password of `phonebook`.

Please note that nested directories are not supported at this time.

## Configuration

After running JPhonebookServer, a configuration file will be created. Edit this as desired, then run the server again.