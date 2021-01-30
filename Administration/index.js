const express = require('express');
const processor = require('./processor.js');
const bodyParser = require('body-parser');
// const multer = require('multer');
// var counter = 0;
// const storage = multer.diskStorage({
// 	destination: function (req, file, cb) {
// 		cb(null, 'public/temp/');
// 	},
// 	filename: function(req, file, cb) {
// 		cb(null, '1.png');
// 	}
// });
// const upload = multer({
// 	storage: storage,
// 	limits: { fieldSize: 50 * 1024 * 1024 }
// });

var app = express();
app.set('view engine', 'ejs');
app.use(bodyParser.json({limit: '50mb'}));
app.use(bodyParser.urlencoded({limit: '50mb', extended: true,
parameterLimit: 100000000}));

app.use('/datamaps', express.static(__dirname + '/node_modules/datamaps/dist/'));

app.use('/public', express.static('public'));
app.use('/resetUsers', processor.resetUserProfile);
app.use('/resetMissing', processor.resetMissingPerson);
app.use('/propagateAlarm', processor.propagateAlarm);
app.use('/processNewMissingRequest', processor.processNewMissingRequest);
app.use('/viewAllMissingRequests', processor.viewAllMissingRequests);
app.use('/viewAllMissingPeople', processor.viewAllMissingPeople);
app.use('/returnAllMissingPeople', processor.returnAllMissingPeople);
app.use('/approveMissingRequest', processor.approveMissingRequest);
app.use('/processAbuseReport', processor.processAbuseReport);
app.use('/resetAlerts',processor.resetAlerts);
app.use('/resetAbuseReports', processor.resetAbuseReports);
app.use('/alertDisplay',processor.alertDisplay);
app.use('/processSetting', processor.processSetting);
app.use('/viewAllSettingRequests', processor.viewAllSettingRequests);
app.use('/resetUserProfileSetting', processor.resetUserProfileSetting);
app.use('/viewAllUsers', processor.viewAllUsers);
app.use('/abuseReportsDisplay', processor.abuseReportsDisplay);
app.use('/banUser', processor.banUserForm);
app.use('/processBanUser', processor.processBanUser);
app.use('/setPropagateDistance', processor.setPropagateDistance);
app.use('/processPropagateDistance', processor.processPropagateDistance);

// Add method (post request from user end)
app.use('/addUserProfile', processor.addUserProfile);
app.use('/addAbuseReport', processor.addAbuseReport);

// Methods on BanUser
app.use('/returnBanUsers', processor.returnBanUsers);
app.use('/resetBanUsers', processor.resetBanUsers);
app.use('/checkBanUser', processor.checkBanUser);
app.use('/returnUsers', processor.returnUsers);
app.use('/updateLocation', processor.updateLocation);
app.use('/returnOneUser', processor.returnOneUser);
app.use('/image', (req, res) => { res.redirect('/public/temp/image.png'); } );
app.use('/', (req, res) => { res.redirect('/public/hello.html'); } );


app.listen(3000,  () => {
	console.log('Listening on port 3000');
});
