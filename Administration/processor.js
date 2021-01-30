const earthRadius = 6378000;
const medicalAlarmCode = 0;
const crimeAlarmCode = 1;
const missingAlarmCode = 2;
const googleRGURL = 'https://maps.googleapis.com/maps/api/geocode/json?latlng=';
const async = require('async');
const twilioClient = require('twilio')(twilioAccountSid, twilioAuthToken);
const UserProfile = require('./database/UserProfile.js');
const UserProfileSetting = require('./database/UserProfileSetting.js');
const MissingPerson = require('./database/MissingPerson.js');
const Alert = require('./database/Alert.js');
const AbuseReport = require('./database/AbuseReport.js');
const BanUser = require('./database/BanUser.js');
const fs = require('fs');
const config = require('./config.js');
const googleMapsClient = require('@google/maps').createClient({
    key: googleAPIKey
});
const https = require('https');
const base64ToImage = require('base64-to-image');

function toRadian(degree) {
    var pi = Math.PI;
    return degree * (pi/180);
}

function reverseGeocode(latlng, callback) {
    var latitude = latlng.lat;
    var longitude = latlng.lng;
    var url = googleRGURL + latitude + ',' + longitude + '&key=' + googleAPIKey;
    console.log('getting url');
    https.get(url, (resp) => {
        let data = '';

        resp.on('data', (chunk) => {
            data += chunk;
        });

        resp.on('end', () => {
            console.log('finished reading data');
            var result = JSON.parse(data);
            if(result && result.status == 'OK') {
                var components = result.results[0].address_components;
                var resultAddress = {};
                components.forEach((component) => {
                    if(component.types[0] == 'street_number') {
                        resultAddress.streetNumber = component.short_name;
                    } else if(component.types[0] == 'route') {
                        resultAddress.streetName = component.short_name;
                    } else if(component.types[0] == 'locality') {
                        resultAddress.city = component.short_name;
                    } else if(component.types[0] == 'administrative_area_level_1') {
                        resultAddress.state = component.short_name;
                    } else if(component.types[0] == 'country') {
                        resultAddress.country = component.short_name;
                    }
                });
                callback(resultAddress);
            } else {
                console.log('Null result from google reverse geocode.');
            }
        });
    }).on("error", (err) => {
        console.log("Error: " + err.message);
    });
}

/*
async function asyncForEach(array, callback) {
  for (let index = 0; index < array.length; index++) {
    await callback(array[index], index, array);
  }
}
*/

var haversineDistance = function (la1, lo1, la2, lo2) {
    var phi1 = toRadian(la1);
    var phi2 = toRadian(la2);
    var deltPhi = toRadian(la2-la1);
    var deltLambda = toRadian(lo2-lo1);

    var a = Math.sin(deltPhi/2) * Math.sin(deltPhi/2) +
            Math.cos(phi1) * Math.cos(phi2) * Math.sin(deltLambda/2) * Math.sin(deltLambda/2);
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return (earthRadius * c);
}

var sendAlarmToPhone = function (phoneNumber, alarmCode, latitude, longitude) {
    console.log('got id ' + phoneNumber);
    reverseGeocode({lat: latitude, lng: longitude}, (address) => {
        console.log('Reverse geocode finished');
        var address = address.streetNumber + ' ' + address.streetName + ', ' + address.city + ', ' + address.state;
        var msg = '[ConnectSentinel Alarm] ';
        if(alarmCode == 0) {
            msg += 'Medical';
        } else if(alarmCode == 1) {
            msg += 'Crime';
        }
        msg += ' alarm sent from ' + address + '. Please help if you can!';
        console.log('Sending message');
        twilioClient.messages.create({
            body: msg,
            to: '+1' + phoneNumber,
            from: '+12674817718'
        }).then((message) => {
            console.log(message);
        });
    });
};

var sendReplyToPhone = function (phoneNumber, message) {
    var msg = '[ConnectSentinel Reply] ';
    msg += message;
    twilioClient.messages.create({
        body: msg,
        to: '+1' + phoneNumber,
        from: '+12674817718'
    }).then((message) => console.log(message.sid));
};

var findPasswordGivenId = function (id, callback) {
  UserProfile.findOne({id: id}).then((doc) =>{
    if (doc) {
      console.log("Password got:" + doc.password);
      callback(doc.password);
    } else {
      callback(null);
    }
  });
};

var sendMissingHelpToPhone = function (phoneNumber, name,
    lastSeenStreet, lastSeenCity, description, contactNumber) {
    var msg = '[ConnectSentinel Missing Person Alarm]' + '\n';
    msg += "Name: " + name + "\n" + "Last seen at: " + lastSeenStreet +
    ", " + lastSeenCity + "\n" + "Description: " + description + "\n" +
    "If you have any information, please contact: " + contactNumber;
    twilioClient.messages.create({
        body: msg,
        to: '+1' + phoneNumber,
        from: '+12674817718'
    }).then((message) => console.log(message.sid));
};

var convertAddress = function (street, city, fn) {
    var address = street + ", " + city;
    googleMapsClient.geocode({address : address} , function(err, response) {
        if (!err && response.json.results.length > 0) {
            fn(response.json.results[0].geometry.location);
        } else if (err) {
            console.log('Got error from google');
        } else {
            console.log('No result from google');
        }
    });
}

var propagateAlarm = function (req, res) {
    console.log("Received");
    var originLa = parseFloat(req.body.latitude);
    var originLo = parseFloat(req.body.longitude);
    var alarmCode = parseInt(req.body.alarmCode);
    var originUser = parseInt(req.body.originID);
    console.log(originLa);
    console.log(originLo);
    var maxDistance = 0;
    var latlng = {lat : originLa, lng : originLo};
    if(alarmCode == 0) {
        maxDistance = config.medicalAlarmDistance;
    } else if(alarmCode == 1) {
        maxDistance = config.crimeAlarmDistance;
    }
    targetUsers(maxDistance, latlng, originUser, (user) => {
        console.log('Sending alarm to ' + user.id);
        sendAlarmToPhone(user.id, alarmCode, originLa, originLo);
    });
    if (alarmCode == crimeAlarmCode || alarmCode == medicalAlarmCode) {
        var type = 'Medical';
        if(alarmCode == crimeAlarmCode) {
            type = 'Criminal'
        }
        var newAlert = new Alert({
            sentTime: new Date(),
            type: type,
            sentId: originUser,
        	propagateDistance: maxDistance,
            latitude: originLa,
            longitude: originLo
        });
        newAlert.save((err) => {
            if(err) {
                console.log('Error when saving a new alert! ' + err);
            } else {
                console.log('Saved new alert');
            }
        });
    }
    res.type('html').status(200);
    res.send('Alarms sent');
};

var setPropagateDistance = function (req, res) {
    var configMedicalDistance = config.medicalAlarmDistance;
    var configCrimeDistance = config.crimeAlarmDistance;
    var configMissingDistance = config.missingPersonDistance;
    res.render('setPropagateDistance',
    { md: configMedicalDistance, cd: configCrimeDistance, mid: configMissingDistance});
}

var processPropagateDistance = function (req, res) {
    console.log('received request');
    var newMedicalDistance = req.body.MedicalDistance;
    var newCrimeDistance = req.body.CrimeDistance;
    var newMissingDistance = req.body.MissingDistance;
    config.medicalAlarmDistance = newMedicalDistance;
    config.crimeAlarmDistance = newCrimeDistance;
    config.missingPersonDistance = newMissingDistance;
    console.log(newMissingDistance);
    res.redirect('/public');
}

var targetUsers = function (maxDistance, latlng, originUser, targetAction) {
    var lat = latlng.lat;
    var lng = latlng.lng;
    UserProfile.find( (err, allUsers) => {
        if (err) {
            res.type('html').status(500);
            res.send('DataBase error encountered.');
        } else {
            console.log('Max Distance is ' + maxDistance);
            allUsers.forEach((user) => {
                if(user.id == originUser) {
                    return;
                }
                console.log('Iterating user ' + user.name);
                var targetLa = user.latitude;
                var targetLo = user.longitude;
                var d = haversineDistance(lat, lng, targetLa, targetLo);
                console.log('Calculated distance ' + d);
                if(d < maxDistance) {
                    console.log('Performing Action.');
                    targetAction(user);
                }
            });
        }
    });
}



var resetUserProfile = function (req, res) {
    UserProfile.deleteMany({}, function(err) {
        if(err) {
            console.log('Error when resetting: ' + err);
            res.send(500, err);
        } else {
            console.log('collection removed');
            var tony = new UserProfile({
                id: 1234560002,
            	name: 'TonyZ',
            	emergencyContactID: 1234560003,
                medicalAbility: true,
                crimeAbility: true,
                latitude: 39.9530886,
                longitude: -75.1931454,
                //tonypw
                password: '019523012596A7ABF01DA0C30C2F20F1484738C6B8C82DFE71FA83F9019302A3'
            });
            var sylvie = new UserProfile({
                id: 2678725611,
            	name: 'Sylvie',
            	emergencyContactID: 1234560002,
                medicalAbility: true,
                crimeAbility: true,
                latitude: 37.4211379,
                longitude: -122.0849253,
                //sylviepw
                password: 'E7DD39FD12DE88A328E93483A903A58CE7F7627EEA715309135E8AAED83713F3'
            });
            var effie = new UserProfile({
                id: 8572720688,
            	name: 'Effie',
            	emergencyContactID: 1234560004,
                medicalAbility: true,
                crimeAbility: true,
                latitude: 39.953306,
                longitude: -75.201095,
                //effiepw
                password: '5A6B0FB1A3E163344EB768B538FF2477D2DED8E6AEFE1F7965C2F720C456C986'
            });
            var chelsie = new UserProfile({
                id: 6463882213,
                name: 'Chelsie',
            	emergencyContactID: 2678725611,
                medicalAbility: true,
                crimeAbility: true,
                latitude: 39.953583,
                longitude: -75.192970,
                //chelsiepw
                password: '447C0801E3CBBAA52C4E2ECF650A1E52F6B2BEB03E699604A4B5E163D0753F13'
            });
            var docArr = [tony, sylvie, effie, chelsie];
            UserProfile.insertMany(docArr, (err, docs) => {
                if(err) {
                    console.log('Failed when inserting. ' + err);
                } else {
                    console.log('Insert succesful.');
                }
            });
            // res.status(200).send('DB reset finished.');
            res.redirect('/viewAllUsers');
        }
    });
}

var viewAllMissingPeople = function (req, res) {
    MissingPerson.find((err, allMissingPeople) => {
        if (err) {
            res.type('html').status(500);
            res.send('Error: ' + err);
        }
        else if (allMissingPeople.length == 0) {
            res.type('html').status(200);
            res.send('There are no missing people');
        }
        else {
            var stateMap = {'AL':0, 'AK':0, 'AR':0, 'AZ':0, 'CA':0, 'CO':0, 'CT':0, 'DE':0,
            'FL':0, 'GA':0, 'HI':0, 'ID':0, 'IL':0, 'IN':0, 'IA':0, 'KS':0, 'KY':0, 'LA':0, 'ME':0, 'MD':0,
            'MA':0, 'MI':0, 'MN':0, 'MS':0, 'MO':0, 'MT':0, 'NE':0, 'NV':0, 'NJ':0, 'NH':0, 'NM':0, 'NY':0,
            'NC':0, 'ND':0, 'OH':0, 'OK':0, 'OR':0, 'PA':0, 'RI':0, 'SC':0, 'SD':0, 'TN':0, 'TX':0, 'UT':0,
            'VT':0, 'VA':0, 'WA':0, 'WV':0, 'WI':0, 'WY':0};
            allMissingPeople.forEach((missingPerson) => {
                if (!missingPerson.isRequest) {
                    stateMap[missingPerson.lastSeenState]++;
                }
            });
            res.render('showAllMissingPeople',
            { missingPeople : allMissingPeople, stateMap : stateMap });
        }
    });

}

var returnAllMissingPeople = function (req, res) {
    MissingPerson.find((err, allMissingPeople) => {
        if (err) {
            res.type('html').status(500);
            res.send('Error: ' + err);
        }
        else if (allMissingPeople.length == 0) {
            res.type('html').status(200);
            res.send('There are no missing people');
        }
        else {
            var returnedMissingPeople = [];
            allMissingPeople.forEach((missingPerson) => {
                if (!missingPerson.isRequest) {
                    returnedMissingPeople.push(missingPerson);
                }
            });
            res.send(returnedMissingPeople);
        }
    });

}

var resetMissingPerson = function (req, res) {
    MissingPerson.deleteMany({}, function(err) {
        if(err) {
            console.log('Error when resetting: ' + err);
            res.send(500, err);
        } else {
            console.log('collection removed');
            var cat = new MissingPerson({
            	name: 'Meow',
            	lastSeenStreet: '3420 Walnut Street',
                lastSeenCity: 'Philadelphia',
                lastSeenState: 'PA',
                description: 'Cute yet fierce',
                contactNumber: 1234560002,
                isRequest: false,
                hasImage: false
            });
            var owl = new MissingPerson({
                name: 'Gu',
                lastSeenStreet: '2 Park Street',
                lastSeenCity: 'Blairstown',
                lastSeenState: 'NJ',
                description: 'Snowy Owl',
                contactNumber: 2155822471,
                isRequest: true,
                hasImage: false
            });
            var knight = new MissingPerson({
                name: 'K',
                lastSeenStreet: '251 Canaan Road',
                lastSeenCity: 'Salisbury',
                lastSeenState: 'CT',
                description: 'Mascot',
                contactNumber: 1234560002,
                isRequest: false,
                hasImage: false
            });
            var missingArr = [cat, owl, knight];
            MissingPerson.insertMany(missingArr, (err, docs) => {
                if(err) {
                    console.log('Failed when inserting. ' + err);
                } else {
                    console.log('Insert succesful.');
                }
            });
            // res.status(200).send('DB reset finished.');
            res.redirect('/viewAllMissingPeople');
        }
    });
}
var counter = 1;
var processNewMissingRequest = function (req, res) {
    console.log("Request received");
    var name = req.body.name;
    var lastSeenStreet = req.body.lastSeenStreet;
    var lastSeenCity = req.body.lastSeenCity;
    var lastSeenState = req.body.lastSeenState;
    var description = req.body.description;
    var contactNumber = req.body.contactNumber;
    var imgData = req.body.picture.split("_").join("/").split("-").join("+");
    console.log(imgData);
    var newMissingRequest = new MissingPerson({
        name: name,
        lastSeenStreet: lastSeenStreet,
        lastSeenCity: lastSeenCity,
        lastSeenState: lastSeenState,
        description: description,
        contactNumber: contactNumber,
        isRequest: true
    });
    if (imgData) {
        newMissingRequest.image = 'public/temp/image' + counter +".png";
        newMissingRequest.hasImage = true;
        newMissingRequest.imageBase64 = imgData;
    } else {
        newMissingRequest.hasImage = false;
    };
    newMissingRequest.save( (err) => {
        if (err) {
            res.type('html').status(500);
            res.send('Error: ' + err);
        }
        else {
            fs.writeFile('public/temp/image' + counter +".png",
            imgData, 'base64',function(err) {
                if(err) {
                    console.log('Error when creating file');
                    console.log(err);
                    res.type('html').status(500);
                    res.send('Error: ' + err);
                } else {
                    console.log('File created');
                    counter++;
                    res.status(200).send();
                }
            });
        }
    });
}

var viewAllMissingRequests = function (req, res) {
    MissingPerson.find((err, allMissingPeople) => {
        if (err) {
            res.type('html').status(500);
            res.send('Error: ' + err);
        }
        else if (allMissingPeople.length == 0) {
            res.type('html').status(200);
            res.send('There are no missing people');
        }
        else {
            res.render('showAllMissingRequests',
            { missingRequests : allMissingPeople});
        }
    });
}

var processAbuseReport = function (req, res) {
    var msg = 'The administrator has reviewed your abuse report:' + req.body.msg;
    var reporterID = req.body.reporterID;
    sendReplyToPhone(reporterID, msg);
    res.redirect('/viewAllUsers');
}

var approveMissingRequest = function (req, res) {
    var requestName = req.body.reportedName;
    var phoneNumber = req.body.reportedPhone;
    var message = req.body.message;
    console.log("Request received");
    MissingPerson.findOne({name : requestName},
    (err, result) => {
        if (err) {
            res.type('html').status(500);
            res.send('Error: ' + err);
        }
        else if (!result) {
            res.type('html').status(200);
            res.send('No missing person named ' + requestName);
        }
        else {
            if (req.body.isAccepted == "true") {
                result.isRequest = false;
                result.save( (err) => {
                    if (err) {
                        res.type('html').status(500);
                        res.send('Error: ' + err);
                    }
                    else {
                        sendReplyToPhone(phoneNumber,
                            'Your request is accepted. ' + message);
                        convertAddress(result.lastSeenStreet,
                        result.lastSeenCity, (location) => {
                            targetUsers(config.missingPersonDistance, location,
                                result.contactNumber, (user) => {
                                    sendMissingHelpToPhone(user.id, result.name,
                                    result.lastSeenStreet, result.lastSeenCity,
                                    result.description, result.contactNumber)
                                });
                        });
                        res.redirect('/viewAllMissingRequests');
                    }
                });
            } else {
                result.remove((err, result) => {
                    if (err) {
                        res.type('html').status(500);
                        res.send('Error: ' + err);
                    }
                    else {
                        sendReplyToPhone(phoneNumber,
                            'Your request is declined. ' + message);
                        res.redirect('/viewAllMissingRequests');
                    }
                })
            }
        }
    });
}

var alertDisplay = function (req, res) {
    Alert.find((err, allAlerts) => {
        if (err) {
            res.type('html').status(500);
            res.send('Error: ' + err);
        }
        else if (allAlerts.length == 0) {
            res.type('html').status(200);
            res.send('There are no alerts available');
        }
        else {
          var alerts2pass = [];
          allAlerts.forEach( (alert) => {
            alerts2pass.push({sentId: alert.sentId, type:alert.type,
                time: alert.sentTime, lat: alert.latitude, lng: alert.longitude});
          });
          res.render('alertsDisplay',
            { alerts : JSON.stringify(alerts2pass)});
          /*
            async.series([ function(cb){
                var alerts2pass = [];
                asyncForEach(allAlerts, async(alert) => {
                  UserProfile.find({id: alert.sentId}, (err, result) => {
                    alerts2pass.push({sentId: alert.sentId, name:result[0].name, lat: alert.latitude, lng: alert.longitude});
                  });
                });
                cb(null, alerts2pass);
              }], function(err, results) {
                console.log(results);
                res.render('alertsDisplay',
                  { alerts : JSON.stringify(results)});
              }); */
          /*  allAlerts.forEach( (alert) => {
              async.series([ function(cb){
                UserProfile.find({id: alert.sentId}, (err, result) => {
                  alerts2pass.push({sentId: alert.sentId, name:result[0].name, lat: alert.latitude, lng: alert.longitude});
                });
                cb(null, "Updated 1");
              }], function(err, results) {
                console.log(results);
              });
            }); */
          }
    });
}

var resetAlerts = function (req, res) {
    Alert.deleteMany({}, function(err) {
        if(err) {
            console.log('Error when resetting: ' + err);
            res.send(500, err);
        } else {
            console.log('collection removed');
            var poison = new Alert({
              sentTime: new Date(),
              type: "Criminal",
              sentId: 1234560001,
          	  propagrateDistance: 100,
              latitude: 37.4211379,
              longitude: -122.0849253
            });
            var poison2 = new Alert({
              sentTime: new Date(),
              type: "Medical",
              sentId: 1234560002,
          	  propagrateDistance: 100,
              latitude: 38.4211379,
              longitude: -122.0849253
            });
            var poison3 = new Alert({
              sentTime: new Date(),
              type:"Criminal",
              sentId: 1234560003,
          	  propagrateDistance: 100,
              latitude: 45.4211379,
              longitude: -110.0849253
            });
            var missingArr = [poison, poison2, poison3];
            Alert.insertMany(missingArr, (err, docs) => {
                if(err) {
                    console.log('Failed when inserting. ' + err);
                } else {
                    console.log('Insert succesful.');
                }
            });
            //res.status(200).send('Alert DB reset finished.');
            res.redirect('/alertDisplay');
        }
    });
}

var resetAbuseReports = function (req, res) {
    AbuseReport.deleteMany({}, function(err) {
        if(err) {
            console.log('Error when resetting: ' + err);
            res.send(500, err);
        } else {
            console.log('collection removed');
            var report1 = new AbuseReport({
              originUserID: 1234560001,
              targetUserID: 1234560002,
              description: "I can't do Coding",
              date: new Date(),
              location: "Towne 100"
            });
            var report2 = new AbuseReport({
              originUserID: 1234560002,
              targetUserID: 1234560003,
              description: "I can't do Math",
              date: new Date(),
              location: "DRL's dark corner"
            });
            var reports = [report1, report2];
            AbuseReport.insertMany(reports, (err, docs) => {
                if(err) {
                    console.log('Failed when inserting. ' + err);
                } else {
                    console.log('Insert succesful.');
                }
            });
            //res.status(200).send('Abuse DB reset finished.');
            res.redirect('/abuseReportsDisplay');
        }
    });
}

var abuseReportsDisplay = function (req, res) {
    AbuseReport.find((err, allAbuseReports) => {
        if (err) {
            res.type('html').status(500);
            res.send('Error: ' + err);
        }
        else if (allAbuseReports.length == 0) {
            res.type('html').status(200);
            res.send('There are no abuse reports');
        }
        else {
            res.render('abuseReportsDisplay',
            { abuseReports : allAbuseReports, reportsJSON : JSON.stringify(allAbuseReports)});
        }
    });
}


var viewAllUsers = function (req, res) {
    UserProfile.find((err, userProfiles) => {
        if (err) {
            res.type('html').status(500);
            res.send('Error: ' + err);
        }
        else if (userProfiles.length == 0) {
            res.type('html').status(200);
            res.send('There are no users');
        }
        else {
            res.render('showAllUsers',
            { users : userProfiles});
        }
    });
}

var returnUsers = function (req, res) {
    UserProfile.find((err, users) => {
    if (err) {
      res.type('html').status(500);
      res.send('Error: ' + err);
    }
    else {
      //res.render('returnUsers');
      res.type('html').status(200);
      res.send(JSON.stringify(users));
    }
  });
}

var returnOneUser = function(req, res) {
   var ID = req.body.Id;
   console.log('userID is: ' + ID);
    UserProfile.findOne({id : ID},
    (err, user) => {
        if (err) {
            console.log('error');
            res.status(500).send('Error: ' + err);
        }
        else if (!user) {
            console.log('error');
            res.status(500).send('No User with ID ' + ID);
        }
        else {
            console.log('found one user');
            res.status(200).send(JSON.stringify(user));
        }
    });
}

var returnBanUsers = function (req, res) {
  BanUser.find((err, users) => {
    if (err) {
      res.type('html').status(500);
      res.send('Error: ' + err);
    }
    else {
      //res.render('returnUsers');
      res.type('html').status(200);
      res.send(JSON.stringify(users));
    }
  });
}

var resetBanUsers = function (req, res) {
  BanUser.deleteMany({}, function(err) {
      if(err) {
          console.log('Error when resetting: ' + err);
          res.send(500, err);
      } else {
          console.log('collection removed');
          var ban1 = new BanUser({
              id: 6543210001,
              password: "ban"
          });
          var ban2 = new BanUser({
              id: 6543210002,
              password: "ban"
          })
          var docArr = [ban1, ban2];
          BanUser.insertMany(docArr, (err, docs) => {
              if(err) {
                  console.log('Failed when inserting. ' + err);
              } else {
                  console.log('Insert succesful.');
              }
          });
          res.status(200).send('DB user setting reset finished.');
      }
  });
}

var resetUserProfileSetting = function (req, res) {
    UserProfileSetting.deleteMany({}, function(err) {
        if(err) {
            console.log('Error when resetting: ' + err);
            res.send(500, err);
        } else {
            console.log('collection removed');
            var tony = new UserProfileSetting({
                id: 1234560002,
                name: 'TonyZheng',
                emergencyContactID: 1234560002,
                medicalAbility: false,
                crimeAbility: true,
                password: 'tonypw'
            });
            var sylvie = new UserProfileSetting({
                id: 2678725611,
                name: 'SylvieSun',
                emergencyContactID: 2678725611,
                medicalAbility: true,
                crimeAbility: false,
                password: 'sylviepw'
            });
            var effie = new UserProfileSetting({
                id: 8572720688,
                name: 'EffieLi',
                emergencyContactID: 1234560003,
                medicalAbility: false,
                crimeAbility: false,
                password: 'effiepw'

            });
            var chelsie = new UserProfileSetting({
                id: 2155822471,
                name: 'ChelsieXu',
                emergencyContactID: 2678725611,
                medicalAbility: true,
                crimeAbility: true,
                password: 'chelsiepw'
            });
            var docArr = [tony, sylvie, effie, chelsie];
            UserProfileSetting.insertMany(docArr, (err, docs) => {
                if(err) {
                    console.log('Failed when inserting. ' + err);
                } else {
                    console.log('Insert succesful.');
                }
            });
            res.status(200).send('DB user setting reset finished.');
        }
    });
}


var viewAllSettingRequests = function (req, res) {
    UserProfileSetting.find((err, allSettings) => {
        if (err) {
            res.type('html').status(500);
            res.send('Error: ' + err);
        }
        else if (allSettings.length == 0) {
            res.type('html').status(200);
            res.send('There are no setting requests');
        }
        else {
            res.render('showAllSettingRequests',
            {settings : allSettings});
        }
    });
}

var banUserForm = function (req, res) {
  res.render('banUserForm', {mes: ""});
}

var processBanUser = function (req, res) {
   var message = req.body.message;
   var id = req.body.userId;
   if ((!message) || (!id)) {
     res.type('html').status(200);
     res.send("Invalid input");
   };
   findPasswordGivenId(id, (password) => {
     console.log(password);
     if (!password) {
       res.type('html').status(200);
       res.send("User Not Found");
     };
       var banUser = new BanUser({
             id: id,
             password: password
       });
       console.log(banUser);
       banUser.save( (err) => {
           if (err) {
               console.log("Error when banning user form the system");
           }
       });
       sendReplyToPhone(id, 'You have been banned from ConnectSentinel: ' + message);
        UserProfile.deleteOne({id: id}).then((result) => {
            res.render('banUserForm', {mes: "User has been deleted."});
        });
        res.render('banUserForm', {mes: "User has been deleted."});
      });
     /*
   }), function(err, user) {
     if (err) {
       console.log('Branch 1');
       res.type('html').status(500);
       res.send('Error when deleting, the error message is: ' + err);
     } else {
       console.log('Branch 2');
      // sendReplyToPhone(id, message);
      // res.alert('Successfully Deleted!!');
       res.render('banUserForm', {mes:"Already Deleted"});
     }
   }); */
};

var addUserProfile = function (req, res) {
    newID = req.body.newID;
    newName = req.body.newName;
    newEmergencyContactID = req.body.newEmergencyContactID;
    newMedicalAbility = req.body.newMedicalAbility;
    newCrimeAbility = req.body.newCrimeAbility;
    newPassword = req.body.newPassword;
    var newUser = new UserProfile({
        id: newID,
        name: newName,
        emergencyContactID: newEmergencyContactID,
        medicalAbility: newMedicalAbility,
        crimeAbility: newCrimeAbility,
        password: newPassword,
        longitude: 0,
        latitude: 0
    });
    newUser.save( (err) => {
        if (err) {
            res.type('html').status(500);
            res.send('Error: ' + err);
        } else {
            res.redirect('/showAllUsers');
        }
    });
}

var addAbuseReport = function (req, res) {
    newOriginID = req.body.originUserID;
    newTargetUserID = req.body.targetUserID;
    newDescription = req.body.description;
    newDate = req.body.date;
    newLoc = req.body.location;
    var newReport = new AbuseReport({
      originUserID: newOriginID,
      targetUserID: newTargetUserID,
      description: newDescription,
      date: newDate,
      location: newLoc
    });
    newReport.save( (err) => {
        if (err) {
            res.type('html').status(500);
            console.log("Error:" + err);
            res.send('Error: ' + err);
        } else {
          res.type('html').status(200);
          res.redirect('/abuseReportsDisplay');
        }
    });
}

var processSetting = function (req, res) {
    console.log(req.body.settingMedicalAbility);
    console.log(req.body.settingCrimeAbility);
    console.log(req.body.settingId);
    var ID = req.body.settingId;
    UserProfile.findOne({id : ID},
    (err, user) => {
        if (err) {
            res.type('html').status(500);
            res.send('Error: ' + err);
        }
        else if (!user) {
            res.type('html').status(200);
            res.send('No User with ID ' + ID);
        }
        else {
            user.name = req.body.settingName;
            user.emergencyContactID = req.body.settingEmergencyContactID;
            user.medicalAbility = req.body.settingMedicalAbility;
            user.crimeAbility = req.body.settingCrimeAbility;
            user.password = req.body.settingPassword;
            user.save ( (err) => {
                if (err) {
                    res.type('html').status(500);
                        res.send('Error: ' + err);
                } else {
                    res.type('html').status(200);
                    res.send('OK');
                }
            });
        }
    });
    // UserProfileSetting.findOne({id : ID},
    // (err, request) => {
    //     if (err) {
    //         res.type('html').status(500);
    //         res.send('Error: ' + err);
    //     }
    //     else if (!request) {
    //         res.type('html').status(200);
    //         res.send('No User with ID ' + ID);
    //     }
    //     else {
    //         request.remove((err, result) => {
    //             if (err) {
    //                 res.type('html').status(500);
    //                 res.send('Error: ' + err);
    //             }
    //         });
    //         res.redirect('/viewAllSettingRequests');
    //     }
    // });
}

var updateLocation = function (req, res) {
    var lat = parseFloat(req.body.latitude);
    var lng = parseFloat(req.body.longitude);
    var id = parseInt(req.body.id);
    console.log(lat);
    console.log(lng);
    console.log(typeof lat);
    console.log(lat + lng);
    console.log(id);
    UserProfile.findOne({id: id}, (err, user) => {
        if(err) {
            console.log("Error when finding user.");
            console.log(err);
            res.status(500).send();
        } else if(!user) {
            console.log("No such user found");
            res.status(500).send();
        } else {
            user.latitude = lat;
            user.longitude = lng;
            user.save((err) => {
                if(err) {
                    console.log("Error when saving updated location.");
                    console.log(err);
                    res.status(500).send();
                } else {
                    res.status(200).send();
                }
            });
        }
    });
}

var checkBanUser = function (req, res) {
  var id = req.body.enterId;
  //console.log(id);
  var pw = req.body.enterPassword;
  BanUser.findOne({id: id}, (err, user) => {
    if(err) {
        console.log("Error");
        console.log(err);
        res.status(500).send("Error");
    } else if(!user) {
        console.log("No");
        res.status(200).send("No");
    } else {
        if (user.password == pw) {
          console.log("Check ban user: Good");
          res.status(200).send("Good");
        } else {
          console.log("Wrong Password");
          res.status(200).send("Wrong Password");
        }
    }
  })
}

var processor = {
    resetUserProfile: resetUserProfile,
    resetMissingPerson: resetMissingPerson,
    propagateAlarm: propagateAlarm,
    processNewMissingRequest: processNewMissingRequest,
    viewAllMissingPeople: viewAllMissingPeople,
    viewAllMissingRequests: viewAllMissingRequests,
    returnAllMissingPeople: returnAllMissingPeople,
    approveMissingRequest: approveMissingRequest,
    resetAlerts: resetAlerts,
    alertDisplay: alertDisplay,
    resetUserProfileSetting: resetUserProfileSetting,
    viewAllSettingRequests : viewAllSettingRequests,
    processSetting : processSetting,
    viewAllUsers: viewAllUsers,
    resetAbuseReports: resetAbuseReports,
    abuseReportsDisplay: abuseReportsDisplay,
    processAbuseReport: processAbuseReport,
    banUserForm: banUserForm,
    processBanUser: processBanUser,
    setPropagateDistance: setPropagateDistance,
    processPropagateDistance: processPropagateDistance,
    returnUsers: returnUsers,
    addUserProfile: addUserProfile,
    returnBanUsers: returnBanUsers,
    resetBanUsers:resetBanUsers,
    updateLocation: updateLocation,
    addAbuseReport: addAbuseReport,
    // Last iteration.
    checkBanUser: checkBanUser,
    returnOneUser: returnOneUser
}

module.exports = processor;
