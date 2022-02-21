<p align="center">
    <img width="70px" src="https://raw.githubusercontent.com/Eerie6560/Archives/main/images/icons/Elixir-Circle.png" align="center" alt="GitHub Readme Stats" />
    <h2 align="center">Elixir</h2>
</p>

<p align="center">
    The official repository for Elixir, an advanced music bot for your Discord server.
</p>

<p align="center">
    <a href="https://ponjo.club/discord">
      <img src="https://img.shields.io/badge/Discord-Join%20for%20support!-blue?style=for-the-badge&logo=discord&logoColor=white" alt=""/>
    </a>
    <a href="https://eerie.codes">
      <img src="https://img.shields.io/badge/Supports%20-OpenJDK%2016+-gray.svg?colorA=61c265&colorB=4CAF50&style=for-the-badge&logo=java&logoColor=white" alt=""/>
    </a>
</p>

### Credits

- Developers: Ben Petrillo (Eerie6560), KingRainbow44 (Magix)

### Hosting

Firstly, clone this repository.

```shell
git clone https://github.com/Eerie6560/Elixir-V4.git
```

- Make sure to install all Maven dependencies using your IDE.
- Fill in ALL values in the configuration file. An example is provided in `.env.example`. If you don't fill all fields properly, the bot will not run.
- To avoid ratelimiting, Elixir uses IPv6 rotation. Note that this requires an entire 64-block IPv6 address. Check with your hosting provider to see if you have one. If not, leave this field blank; however, you may run into ratelimiting issues.

### All Features

- [x] Support for YouTube and Spotify URLs (playlists included).
- [x] Ability to queue YouTube playlists with up to 600 tracks, Spotify playlists with up to 2,000 tracks. 
- [x] Fully integrated slash commands.
- [x] Loop singular tracks & the entire music queue.
- [x] View the track currently playing, its cover art, artist, duration, etc.
- [x] Fully functional custom playlist system.
    - [x] Custom playlist titles, cover art images, descriptions.
    - [x] Settings (automatically loop, automatic custom volume, title, description).
    - [x] Manually add or remove tracks based on their queue position.
    - [x] Fetch all tracks in any custom playlist.
    - [x] Global system (play anyone else's track).
    - [x] Ability to delete your custom playlists (you cannot delete or modify someone else's).
- [x] Modify player volume.
- [x] Shuffle the guild queue.
- [x] View all tracks in the guild queue.
- [x] Loop a particular track or queue.
- [x] Fetch lyrics of any track.

### API Usage

Elixir Music has its own public API! To view its documentation, [click here](https://docs.ponjo.club).

### License

© Copyright 2021-2022 Ben Petrillo, KingRainbow44. All rights reserved.

Elixir is licensed under the [MIT License](https://www.mit.edu/~amini/LICENSE.md), and modification is permitted provided that proper credit is given to the original author(s). Under no circumstances should this repository be redistributed withount permission from the original author. 
