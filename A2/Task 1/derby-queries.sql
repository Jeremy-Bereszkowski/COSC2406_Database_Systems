-- Top 5 sensors by hourly counts on 30/August/2012
select sensors.sensor_name, recordings.time, recordings.hourly_counts from sensors
join sensors_recordings on sensors.sensor_id = sensors_recordings.sensor_id
join recordings on recordings.recording_id = sensors_recordings.recording_id
where recordings.mYear = 2012 and recordings.mMonth = 'August' and recordings.mDate = 30
order by recordings.hourly_counts desc
fetch first 5 rows only;

-- Total counts by sensor #1 on 30/August/2012
select sensors.sensor_id, sensors.sensor_name, SUM(recordings.hourly_counts) as Total_Daily_Counts from sensors
join sensors_recordings on sensors.sensor_id = sensors_recordings.sensor_id
join recordings on recordings.recording_id = sensors_recordings.recording_id
where recordings.mYear = 2012 and recordings.mMonth = 'August' and recordings.mDate = 30 and sensors.sensor_id = '1'
group by sensors.sensor_id, sensors.sensor_name;

-- Top 10 sesnors by total counts
select sensors.sensor_id, sensors.sensor_name, SUM(recordings.hourly_counts) as Total_Counts from sensors
join sensors_recordings on sensors.sensor_id = sensors_recordings.sensor_id
join recordings on recordings.recording_id = sensors_recordings.recording_id
group by sensors.sensor_id, sensors.sensor_name
order by Total_Counts desc
fetch first 10 rows only;

-- Single greatest hourly count
select sensors.sensor_id, sensors.sensor_name, recordings.mYear, recordings.mMonth, recordings.mDate, recordings.time, recordings.hourly_counts from sensors
join sensors_recordings on sensors.sensor_id = sensors_recordings.sensor_id
join recordings on recordings.recording_id = sensors_recordings.recording_id
order by recordings.hourly_counts desc
fetch first 1 rows only;

create index dateIndex on recordings(mYear, mMonth, mDate);
drop index dateIndex;

connect 'jdbc:derby:A2';
call SYSCS_UTIL.SYSCS_SET_RUNTIMESTATISTICS(1);
call SYSCS_UTIL.SYSCS_SET_STATISTICS_TIMING(1);
MaximumDisplayWidth 5000;

values SYSCS_UTIL.SYSCS_GET_RUNTIMESTATISTICS();
