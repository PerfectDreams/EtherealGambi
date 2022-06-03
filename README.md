# EtherealGambi

Alternative to [DreamStorageService](https://github.com/PerfectDreams/DreamStorageService), but instead of storing files on a database, it is stored on the file system itself.

I made this because most image optimization pipelines (like Cloudflare Images) are SO EXPENSIVE, OMG! I would gladly pay for a good service, but CF Images charges per image served, even if the image was cached!

I didn't want to use DreamStorageService because I wanted a way to host images for Loritta's blog posts in an easier way, just dragging and droppin' the file.

Maybe this project could replace DreamStorageService! Or maybe DreamStorageService will have features that will make this project obsolete... who knows! The fun part is playing around and figuring out what sticks.

So I decided to make my own solution for this, because I just wanted an easy way to store *optimized* images without me caring about expenses and stuff like that.

* Ethereal: extremely delicate and light in a way that seems too perfect for this world.
* Gambi(arra): Workaround because I got fed up with this issue.

## Differences between DreamStorageService and EtherealGambi

* EtherealGambi keeps the original files on the disk
    * DreamStorageService only keeps the already optimized image on the database.
    * While this is not a huge deal if you are dealing with user generated content, it is a pain if you want to keep a copy of the original version of the file.
* EtherealGambi is easier to upload and manage file, because it is *literally* a `files` folder.
    * DreamStorageService keeps the files on the database, so you can't "interact" with the files easily.
* No need to upload images via an API, just drag and drop to a folder.
    * DreamStorageService requires an API for this. This is actually is a disadvantage to EtherealGambi, if you want to upload dynamic/user generated content.
* EtherealGambi only supports resizing images to preset sizes.
    * DreamStorageService supports dynamic image sizes and image cropping.

DreamStorageService is better for user generated content, EtherealGambi is better for static content (example: blog posts' images).

Yes, there is a big overlap in functionality between the two... maybe in the future they could be merged to have the best of both worlds. :3

## Objectives

* Store files
* Optimize images automatically
* Easy to upload files: Just a folder that you can drag files to it

## Should I use this?

no, it is super hacky.