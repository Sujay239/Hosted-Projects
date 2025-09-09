const express = require('express');
const router = express.Router();
const db = require('../db'); // adjust path as needed

router.get('/', async (req, res) => {
    try {
        const [rows] = await db.query('SELECT id, username FROM users WHERE is_online = 1');
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: 'Database error' });
    }
});

module.exports = router;
