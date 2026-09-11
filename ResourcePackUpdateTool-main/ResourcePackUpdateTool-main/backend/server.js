const express = require('express');
const multer = require('multer');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const yaml = require('yaml');
const cors = require('cors');


class resourceprovider {
  constructor() {
    this.config = this.loadconf();
    this.app = express();
    this.upload = this.multersetup();
    this.authmidware();
    this.getroute();
  }

  loadconf() {
    try {
      const configPath = path.join(__dirname, 'config.yaml');
      if (!fs.existsSync(configPath)) {
        throw new Error('config.yaml not found');
      }
      
      const configFile = fs.readFileSync(configPath, 'utf8');
      const config = yaml.parse(configFile);
      
      this.checkpath(config.paths);
      
      return config;
    } catch (error) {
      console.error('config error,', error.message);
      process.exit(1);
    }
  }

  checkpath(paths) {
    const dirs = [paths.uploadDir, path.dirname(paths.resourceFile)];
    
    dirs.forEach(dir => {
      if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
        console.log(`resources vol not found, created ${dir}`);
      }
    });
  }

  multersetup() {
    const storage = multer.diskStorage({
      destination: (req, file, cb) => {
        cb(null, this.config.paths.uploadDir);
      },
      filename: (req, file, cb) => {
        cb(null, 'resource.zip');
      }
    });

    return multer({ 
      storage: storage,
      fileFilter: (req, file, cb) => {
        if (file.mimetype === 'application/zip' || 
            file.mimetype === 'application/x-zip-compressed' ||
            path.extname(file.originalname).toLowerCase() === '.zip') {
          cb(null, true);
        } else {
          cb(new Error('bad file type'), false);
        }
      },
      limits: {
        fileSize: 100 * 1024 * 1024 
      }
    });
  }

  authmidware() {
    this.app.use(cors());
    this.app.use(express.json());
    this.app.use(express.urlencoded({ extended: true }));
  }

  authm(req, res, next) {
    const authHeader = req.headers['authorization'];
    
    if (!authHeader) {
      return res.status(401).json({
        success: false,
        message: 'need authorization'
      });
    }

    const token = authHeader.startsWith('Bearer ') 
      ? authHeader.slice(7) 
      : authHeader;

    try {
      const tokentoverify = this.config.security.token;
      const algorithm = this.config.security.authmethod
    
      const hash = crypto.createHash(algorithm);
      const calcedhash = hash.update(tokentoverify).digest('hex');
      
      if (token !== calcedhash) {
        return res.status(403).json({
          success: false,
          message: 'bad token'
        });
      }
      
      next();
    } catch (error) {
      console.error('Auth error:', error);
      return res.status(500).json({
        success: false,
        message: 'error when auth'
      });
    }
  }

  allresponse(res, success, message, data = null) {
    const response = {
      success,
      message,
      timestamp: new Date().toISOString()
    };
    
    if (data) {
      response.data = data;
    }
    
    res.json(response);
  }

  getroute() {
    this.app.post('/api/v1/upload', this.authm.bind(this), this.upload.single('file'), this.handleUpload.bind(this));
    this.app.get('/api/v1/down', this.handleDownload.bind(this));
    this.app.get('/api/v1/status', this.handleStatus.bind(this));
    this.app.post('/api/v1/rmfile', this.authm.bind(this), this.handleRmfile.bind(this));
    this.app.use(this.errorHandler.bind(this));
  }

  async handleRmfile(req, res) {
  try {
    const filePath = this.config.paths.resourceFile;
    
    if (!fs.existsSync(filePath)) {
      return this.allresponse(res, false, 'no need');
    }

    const fileStats = fs.statSync(filePath);
    const fileInfo = {
      filename: 'resource.zip',
      size: fileStats.size,
      sizeFormatted: this.formatFileSize(fileStats.size),
      lastModified: fileStats.mtime
    };

    await new Promise((resolve, reject) => {
      fs.unlink(filePath, (err) => {
        if (err) {
          reject(err);
        } else {
          resolve();
        }
      });
    });

    if (fs.existsSync(filePath)) {
      return this.allresponse(res, false, 'fail');
    }

    this.allresponse(res, true, 'success', {
      deletedFile: fileInfo,
      deletedAt: new Date().toISOString()
    });

  } catch (error) {
    console.error('error:', error);
    
    let errorMessage = 'error';
    switch (error.code) {
      case 'EBUSY':
        errorMessage = 'EBUSY';
        break;
      case 'EPERM':
        errorMessage = 'EPERM';
        break;
      case 'ENOENT':
        errorMessage = 'ENOENT';
        break;
      case 'EACCES':
        errorMessage = 'EACCES';
        break;
      default:
        errorMessage = `error: ${error.message}`;
    }

    this.allresponse(res, false, errorMessage);
  }
  }

  async handleUpload(req, res) {
    try {
      if (!req.file) {
        return this.allresponse(res, false, 'empty');
      }

      const tempPath = req.file.path;
      const targetPath = this.config.paths.resourceFile;

      fs.renameSync(tempPath, targetPath);

      const stats = fs.statSync(targetPath);

      this.allresponse(res, true, 'success', {
        filename: 'resource.zip',
        size: stats.size,
        updatedAt: stats.mtime
      });

    } catch (error) {
      console.error('Upload error:', error);
      this.allresponse(res, false, 'fail');
    }
  }

  handleDownload(req, res) {
    try {
      const filePath = this.config.paths.resourceFile;
      
      if (!fs.existsSync(filePath)) {
        return res.status(404).json({
          success: false,
          message: 'file not found'
        });
      }

      res.setHeader('Content-Type', 'application/zip');
      res.setHeader('Content-Disposition', 'attachment; filename=resource.zip');
      
      const fileStream = fs.createReadStream(filePath);
      fileStream.pipe(res);

    } catch (error) {
      console.error('download error:', error);
      res.status(500).json({
        success: false,
        message: 'download fail'
      });
    }
  }

  handleStatus(req, res) {
    try {
      const filePath = this.config.paths.resourceFile;
      
      if (!fs.existsSync(filePath)) {
        return this.allresponse(res, false, 'no file exist');
      }

      const stats = fs.statSync(filePath);
      
      this.allresponse(res, true, 'success', {
        filename: 'resource.zip',
        size: stats.size,
        sizeFormatted: this.formatFileSize(stats.size),
        updatedAt: stats.mtime,
        updatedAtFormatted: stats.mtime.toLocaleString('zh-CN')
      });

    } catch (error) {
      console.error('Status error:', error);
      this.allresponse(res, false, 'fail');
    }
  }

  formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  errorHandler(err, req, res, next) {
    console.error('Server error:', err);

    if (err instanceof multer.MulterError) {
      if (err.code === 'LIMIT_FILE_SIZE') {
        return res.status(413).json({
          success: false,
          message: 'too large'
        });
      }
    }

    res.status(500).json({
      success: false,
      message: 'internal error'
    });
  }


  start() {
    const port = this.config.server.port || 3000;
    const host = this.config.server.host || 'localhost';
    
    this.app.listen(port, host, () => {
      console.log(`Running on http://${host}:${port}`);
    });
  }
}

const rwm = new resourceprovider();
rwm.start();