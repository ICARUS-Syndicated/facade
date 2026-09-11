# ResourcePackUpdateTool
A web app for easily providing resource pack for Minecraft<br>
## ToDo
- check information from .mcpack file
- build with docker
- auto gen config.yaml
## Deployment
### For backend
* run `npm install` to install dependences
* Then simply run `node server.js`or you can `npm run start` for production environment
* You can also use docker
### For frontend
* change your server addr on `<a href="http://localhost:21856/api/v1/down" class="button outlined" id="download-btn">`in index.html and `const API_BASE = 'http://localhost:21856/api/v1';`in script.js
* deploy them on nginx or any webserver you want to use