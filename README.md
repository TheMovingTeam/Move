# Move
Move is a data driven transit app for android built to be fast, extendable, and extremely usable.

Although it's currently a work in progress...

## How does it work?
The app pulls the metadata, stops, lines, and other resources for transit providers from a remote repository, which can be any web server, to maintain an up to date database of the services you select stored in your device.

It also includes the format for requesting ETAs and how to parse the response so that it can get the latest data from your local transit provider without any need for a server in the middle.

## Roadmap
### MVP
- [x] User data storage
- [ ] Cache lines and stop data
- [ ] Finalize UI
- [ ] Finalize data specification
- [ ] Implement provider generator (a web crawler to get the latest metadata from each provider)
- [ ] Implement first provider
### Planned
- [ ] QR Scanner
- [ ] Onboarding experience
- [ ] Map routes
- [ ] Document and publish specification
- [ ] Journey page when on a trip
- [ ] Provider alerts
