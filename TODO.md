# TODO

## Next hacking session

- Make sample copkg complete with start.py, stop.py and status.py
- Figure out how to version start.py/stop.py/status.py
- Implement JobRunner
- Implement daemon mode
- Add Basic auth to daemon mode

## Soon

- Make Debian package

## Rest

- Support for "list" command
- Add MD5 checksum for all files in a package
- Support for listing packages available from package repository
- Support for multiple concurrent downloads
- Support for resuming paused/interrupted download
- Support for graceful cancelling of download
- Support for multiple software repositories

## Package integrity

- Add verification step to the unpacking process
- verify checksums
- ensure required files are present

## Sanity checking
- Have some form of sanity checking on Package Coordinates.  Mostly to
  make sure people do not come up with divergent schemes.

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
      
