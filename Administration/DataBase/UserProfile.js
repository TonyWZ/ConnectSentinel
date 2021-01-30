var mongoose = require('mongoose');

option = { useNewUrlParser: true };
mongoose.connect("mongodb+srv://zweichen:G2ITBTCES@cluster0-75ahh.mongodb.net/test?retryWrites=true", option).then(
    () => { console.log('Successfully connected to MongoDB!'); },
    err => { console.log('Error when connecting to MongoDB! ' + err); }
);

var Schema = mongoose.Schema;

var userProfileSchema = new Schema({
    id: {type: Number, required: true, unique: true},
	name: String,
	emergencyContactID: Number,
    medicalAbility: Boolean,
    crimeAbility: Boolean,
    latitude: Number,
    longitude: Number,
    password: {type: String, required: true, unique: false}
});

module.exports = mongoose.model('UserProfile', userProfileSchema);
