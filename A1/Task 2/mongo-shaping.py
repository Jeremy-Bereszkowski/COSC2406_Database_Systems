import csv
import json

with open('data.csv', 'r') as file:
    reader = csv.reader(file, delimiter=',')
    data = {}
    i = 0
    for row in reader:
        if i == 0:
            i = 1    
            continue

        key = row[7]
        
        if not data.has_key(key):
            data[key] = {
                'Sensor_Name': row[8],
                'data': [{
                        'ID': row[0],
                        'Year': row[2],
                        'Month': row[3],
                        'Mdate': row[4],
                        'Day': row[5],
                        'Time': row[6],
                        'Hourly_Counts': row[9]
                    }]
            }
        else:
            data[key]['data'].append({
                        'ID': row[0],
                        'Year': row[2],
                        'Month': row[3],
                        'Mdate': row[4],
                        'Day': row[5],
                        'Time': row[6],
                        'Hourly_Counts': row[9]
                    })

    string = "["
    i = 0

    for key in data:
        if i == 0:
            string += '\n\t{\"' + key + '\":' + json.dumps(data[key]) + "}"
        else:
            string += ',\n\t{\"' + key + '\":' + json.dumps(data[key]) + "}"
        
        i = 1

    string += "\n]"

    with open("data.json", 'w') as jsonf:
        jsonf.write(string)

    print('done')
