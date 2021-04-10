import csv
import json

with open('data.csv', 'r') as file:
    reader = csv.reader(file, delimiter=',')
    sensors = {}
    recordings = []
    sensors_recordings = []

    i = 0
    for row in reader:
        if i == 0:
            i = 1    
            continue

        recording_ID = row[0]
        year = row[2]
        month = row[3]
        mDate = row[4]
        day = row[5]
        time = row[6]
        hourly_Counts = row[9]

        sensor_id = row[7]
        sensor_Name = row[8]

        if sensor_id not in sensors:
            sensors[sensor_id] = sensor_Name
        
        recordings.append([recording_ID, year, month, mDate, day, time, hourly_Counts])
        sensors_recordings.append([sensor_id, recording_ID])

    sensorsString = ""
    recordingsString = ""
    sensorsRecordingsString = ""

    for key in sensors:
        sensorsString += key + "," + sensors[key] + "\n"

    
    for recording in recordings:
        i = 0

        for item in recording:
            if i ==0:
                recordingsString += item
            else:
                recordingsString += "," + item

            i = 1

        recordingsString += "\n"


    for sensorRecording in sensors_recordings:
        i = 0

        for item in sensorRecording:
            if i ==0:
                sensorsRecordingsString += item
            else:
                sensorsRecordingsString += "," + item

            i = 1

        sensorsRecordingsString += "\n"

    with open("derby-sensors.csv", 'w') as jsonf:
        jsonf.write(sensorsString)

    with open("derby-recordings.csv", 'w') as jsonf:
        jsonf.write(recordingsString)

    with open("derby-sensors-recordings.csv", 'w') as jsonf:
        jsonf.write(sensorsRecordingsString)

    print('done')
