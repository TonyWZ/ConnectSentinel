var mongoose = require('mongoose');

option = { useNewUrlParser: true };
mongoose.connect("mongodb+srv://zweichen:G2ITBTCES@cluster0-75ahh.mongodb.net/test?retryWrites=true", option).then(
    () => { console.log('Successfully connected to MongoDB!'); },
    err => { console.log('Error when connecting to MongoDB! ' + err); }
);

var Schema = mongoose.Schema;

var abuseReportSchema = new Schema({
    originUserID: {type: Number, required: true},
	  targetUserID: {type: Number, required: true},
	  description: {type: String, required: true},
    date: Date,
    location: String
});

module.exports = mongoose.model('AbuseReport', abuseReportSchema);
