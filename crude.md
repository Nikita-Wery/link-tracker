Этот файл является сырой статисткой без преобразования в красивый md.
Был взят из результатов логов k6.

THRESHOLDS

    http_req_duration
    ✓ 'p(99)<1000' p(99)=144.3ms

    http_req_failed
    ✓ 'rate<0.01' rate=0.00%


TOTAL RESULTS

    checks_total.......: 7272    119.443144/s
    checks_succeeded...: 100.00% 7272 out of 7272
    checks_failed......: 0.00%   0 out of 7272

    ✓ GET status 200
    ✓ POST status 200

    HTTP
    http_req_duration..............: avg=47.87ms min=5.08ms med=30.24ms max=5.13s p(90)=64ms  p(95)=82.54ms
      { expected_response:true }...: avg=47.87ms min=5.08ms med=30.24ms max=5.13s p(90)=64ms  p(95)=82.54ms
    http_req_failed................: 0.00%  0 out of 7272
    http_reqs......................: 7272   119.443144/s

    EXECUTION
    iteration_duration.............: avg=5.86s   min=2.24s  med=5.45s   max=9.85s p(90)=9.12s p(95)=9.4s   
    iterations.....................: 72     1.182605/s
    vus............................: 2      min=1         max=8
    vus_max........................: 8      min=8         max=8
time="2026-05-22T16:43:37Z" level=error msg="failed to handle the end-of-test summary" error="Could not save some summary information:\n\t- could not open '/perf-tests/scripts/result.json': open /perf-tests/scripts/result.json: no such file or directory"

    NETWORK
    data_received..................: 103 MB 1.7 MB/s
    data_sent......................: 669 kB 11 kB/s




running (1m00.9s), 0/8 VUs, 72 complete and 0 interrupted iterations
load_test ✓ [ 100% ] 0/8 VUs  1m0s


######### WITH CACHE ###########


THRESHOLDS

    http_req_duration
    ✓ 'p(99)<1000' p(99)=47.67ms

time="2026-05-22T18:16:10Z" level=error msg="failed to handle the end-of-test summary" error="Could not save some summary information:\n\t- could not open '/scripts/result.json': open /scripts/result.json: permission denied"
http_req_failed
✓ 'rate<0.01' rate=0.00%


TOTAL RESULTS

    checks_total.......: 27573   452.654058/s
    checks_succeeded...: 100.00% 27573 out of 27573
    checks_failed......: 0.00%   0 out of 27573

    ✓ GET status 200
    ✓ POST status 200

    HTTP
    http_req_duration..............: avg=4.77ms min=424.02µs med=3.06ms max=450.5ms p(90)=7.5ms p(95)=10.57ms
      { expected_response:true }...: avg=4.77ms min=424.02µs med=3.06ms max=450.5ms p(90)=7.5ms p(95)=10.57ms
    http_req_failed................: 0.00%  0 out of 27573
    http_reqs......................: 27573  452.654058/s

    EXECUTION
    iteration_duration.............: avg=1.5s   min=1.13s    med=1.51s  max=2.19s   p(90)=1.72s p(95)=1.8s   
    iterations.....................: 273    4.481723/s
    vus............................: 1      min=1          max=8
    vus_max........................: 8      min=8          max=8

    NETWORK
    data_received..................: 389 MB 6.4 MB/s
    data_sent......................: 2.5 MB 42 kB/s




running (1m00.9s), 0/8 VUs, 273 complete and 0 interrupted iterations
load_test ✓ [ 100% ] 0/8 VUs  1m0s
