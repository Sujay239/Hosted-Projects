const express = require('express');
const app = express();
const onlineUsersRouter = require('./routes/onlineUsers');

app.use('/api/online-users', onlineUsersRouter);

// ...existing code...

module.exports = app;