var mongoose = require('mongoose');

option = { useNewUrlParser: true };
mongoose.connect("mongodb+srv://zweichen:G2ITBTCES@cluster0-75ahh.mongodb.net/test?retryWrites=true", option).then(
    () => { console.log('Successfully connected to MongoDB!'); },
    err => { console.log('Error when connecting to MongoDB! ' + err); }
);

var Schema = mongoose.Schema;

// For now type is either medical or criminal
var alertSchema = new Schema({
    sentTime: Date,
    type: String,
    sentId: {type: Number, required: true},
	  propagateDistance: Number,
    latitude: Number,
    longitude: Number
});

module.exports = mongoose.model('Alert', alertSchema);
