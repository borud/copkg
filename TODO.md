# TODO

## Commands

List of commands that we want to implement in command line utility.

- `install {coordinate}` - Installs package given by `{coordinate}`.
- `uninstall {coordinate}` - Uninstalls package given by `{coordinate}`.
- `list` - list installed packages

## Package integrity
- Add MD5 checksum for all files in a package
- Add verification step to the unpacking process
  - verify checksums
  - ensure required files are present
  
## Download
- Support for authentication (Basic-auth?  What does Maven use?)
- Support for multiple concurrent downloads
- Support for resuming paused/interrupted download
- Support for graceful cancelling of download
- Support for multiple software repositories
- Support for listing packages available from package repository

## Extra features
- Support for bundles.  A bundle is a meta-package that represents several packages.  
  The purpose of a bundle is to make it easier to install entire systems 
  with just one command eg.:
  
  `copkg install com.comoyo:svod-bundle:2.0`
  
- Support for exporting a list of installed packages as list or as a bundle artifact.
  This can be used to clone setups or to export setups easily as bundles.  Eg.:
  
  - List installed packages and ssh to otherhost and install the list of packages there.

      `copkg list | ssh otherhost copkg install -`
        
  - List installed packages and use the package list to create a bundle artifact.
  
      `copkg list -bundle com.comoyo:svod-snapshot-bundle:2.3.4`
      
