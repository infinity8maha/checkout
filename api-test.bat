@echo off
setlocal enabledelayedexpansion
chcp 65001 > nul

:: 设置基本变量
set API_BASE=http://localhost:8080/api
set PASS_COUNT=0
set FAIL_COUNT=0
set TOTAL_TESTS=0
set CART_ID=
set PRODUCT_ID=
set ERROR_OCCURRED=0

:: 创建日志文件
set LOG_FILE=api-test-results.txt
echo API Test Results - %date% %time% > %LOG_FILE%
echo ============================== >> %LOG_FILE%

:: 从main入口开始执行 - 将主流程移到脚本开头，防止标签被当作代码执行
goto :main

:: 测试辅助函数
:test_endpoint
set /a TOTAL_TESTS+=1
echo.
echo [TEST] %~1
echo [TEST] %~1 >> %LOG_FILE%

:: 使用临时文件存储响应内容和状态码
set TEMP_RESPONSE=temp_response.json
set TEMP_HEADERS=temp_headers.txt

:: 为JSON数据创建临时文件
if "%~5"=="" (
    :: 执行curl命令，保存响应和头信息
    curl -s -o "%TEMP_RESPONSE%" -D "%TEMP_HEADERS%" %~2
) else (
    :: 如果提供了JSON数据，则写入临时文件并使用@file方式提交
    echo %~5 > temp_data.json
    curl -s -o "%TEMP_RESPONSE%" -D "%TEMP_HEADERS%" %~2 -d @temp_data.json
    del temp_data.json >nul 2>&1
)

:: 检查curl是否成功执行
if %errorlevel% neq 0 (
    echo [FAILED] - curl error: %errorlevel%
    echo [FAILED] curl error: %errorlevel% >> %LOG_FILE%
    set /a FAIL_COUNT+=1
    set ERROR_OCCURRED=1
    goto :test_end
)

:: 从头信息中提取状态码
set STATUS_CODE=000
for /f "tokens=2 delims= " %%a in ('findstr /B "HTTP" "%TEMP_HEADERS%" 2^>nul') do (
    set STATUS_CODE=%%a
    goto :got_status
)
:got_status

:: 检查状态码是否符合预期
set EXPECTED_STATUS=%~3
if not defined EXPECTED_STATUS set EXPECTED_STATUS=200

if "!STATUS_CODE!"=="%EXPECTED_STATUS%" (
    echo [SUCCESS] - Status: !STATUS_CODE!
    echo [SUCCESS] Status: !STATUS_CODE! >> %LOG_FILE%
    set /a PASS_COUNT+=1
    
    :: 执行可选的验证函数，但不允许它中断流程
    if not "%~4"=="" (
        call :%~4 2>nul || (
            echo [WARNING] - Validation function %~4 failed but continuing
            echo [WARNING] Validation function %~4 failed but continuing >> %LOG_FILE%
        )
    )
) else (
    echo [FAILED] - Status: !STATUS_CODE! (Expected: %EXPECTED_STATUS%)
    echo [FAILED] Status: !STATUS_CODE! (Expected: %EXPECTED_STATUS%) >> %LOG_FILE%
    set /a FAIL_COUNT+=1
    set ERROR_OCCURRED=1
)

:test_end
:: 记录响应到日志
echo Response: >> %LOG_FILE%
type "%TEMP_RESPONSE%" 2>nul >> %LOG_FILE% || echo No response content >> %LOG_FILE%
echo. >> %LOG_FILE%
echo ------------------------------ >> %LOG_FILE%

:: 确保临时文件存在
type nul > "%TEMP_RESPONSE%" 2>nul
type nul > "%TEMP_HEADERS%" 2>nul

goto :eof

:: 提取商品ID函数
:extract_product_id
for /f "tokens=2 delims=:," %%a in ('type "%TEMP_RESPONSE%" ^| findstr "\"id\""') do (
    set PRODUCT_ID=%%a
    set PRODUCT_ID=!PRODUCT_ID:"=!
    set PRODUCT_ID=!PRODUCT_ID: =!
    echo Extracted product ID: !PRODUCT_ID!
    goto :extract_product_id_end
)
:extract_product_id_end
goto :eof

:: 提取购物车ID函数
:extract_cart_id
for /f "tokens=2 delims=:," %%a in ('type "%TEMP_RESPONSE%" ^| findstr "\"id\""') do (
    set CART_ID=%%a
    set CART_ID=!CART_ID:"=!
    set CART_ID=!CART_ID: =!
    echo Extracted cart ID: !CART_ID!
    goto :extract_cart_id_end
)
:extract_cart_id_end
goto :eof

:: 主测试流程开始
:start_tests
echo.
echo Starting API tests...
echo ============================== 

:: 测试健康检查接口
call :test_endpoint "Health Check" "%API_BASE%/test/hello -X GET" "200"

:: ======= 客户API测试 =======
call :test_endpoint "Get All Products" "%API_BASE%/customer/products -X GET" "200"

:: 创建新购物车
call :test_endpoint "Create Cart" "%API_BASE%/customer/cart -X POST" "201" "extract_cart_id"

:: 如果获取购物车ID成功，测试添加商品到购物车
if defined CART_ID (
    if "!CART_ID!"=="" (
        echo [WARNING] - Cart ID is empty, skipping cart tests
    ) else (
        :: 先获取一个产品ID
        call :test_endpoint "Get Product Details" "%API_BASE%/customer/products/1 -X GET" "200" "extract_product_id"
        
        if defined PRODUCT_ID (
            if "!PRODUCT_ID!"=="" (
                echo [WARNING] - Product ID is empty, skipping cart item tests
            ) else (
                :: 添加商品到购物车
                call :test_endpoint "Add Product to Cart" "%API_BASE%/customer/cart/!CART_ID!/items -X POST -H \"Content-Type: application/json\"" "200" "" "{\"productId\":!PRODUCT_ID!,\"quantity\":2}"
                
                :: 获取购物车内容
                call :test_endpoint "Get Cart Contents" "%API_BASE%/customer/cart/!CART_ID! -X GET" "200"
            )
        ) else (
            echo [WARNING] - Product ID not found, skipping cart item tests
        )
    )
) else (
    echo [WARNING] - Cart ID not found, skipping cart tests
)

:: ======= 管理员API测试 =======
:: 管理员登录验证
call :test_endpoint "Admin Login" "%API_BASE%/admin/products -X GET -u admin:admin" "200"

:: 创建新产品
call :test_endpoint "Create Product" "%API_BASE%/admin/products -X POST -H \"Content-Type: application/json\" -u admin:admin" "201" "extract_product_id" "{\"name\":\"Test Product\",\"price\":99.9,\"description\":\"API test product\"}"

:: 如果创建产品成功，测试更新和删除
if defined PRODUCT_ID (
    if "!PRODUCT_ID!"=="" (
        echo [WARNING] - Product ID is empty, skipping product update/delete tests
    ) else (
        :: 更新产品
        call :test_endpoint "Update Product" "%API_BASE%/admin/products/!PRODUCT_ID! -X PUT -H \"Content-Type: application/json\" -u admin:admin" "200" "" "{\"name\":\"Updated Test Product\",\"price\":88.8,\"description\":\"Updated API test product\"}"
        
        :: 获取更新后的产品
        call :test_endpoint "Get Updated Product" "%API_BASE%/admin/products/!PRODUCT_ID! -X GET -u admin:admin" "200"
        
        :: 删除产品
        call :test_endpoint "Delete Product" "%API_BASE%/admin/products/!PRODUCT_ID! -X DELETE -u admin:admin" "204"
        
        :: 验证产品已删除
        call :test_endpoint "Verify Product Deletion" "%API_BASE%/admin/products/!PRODUCT_ID! -X GET -u admin:admin" "404"
    )
) else (
    echo [WARNING] - Failed to create product, skipping product update/delete tests
)

:: 测试折扣API (如果存在)
call :test_endpoint "Get All Discounts" "%API_BASE%/admin/discounts -X GET -u admin:admin" "200"

goto :generate_report

:: 测试完成，生成报告
:generate_report
echo.
echo ============================== 
echo TEST SUMMARY:
echo Total tests: %TOTAL_TESTS%
echo Passed: %PASS_COUNT%
echo Failed: %FAIL_COUNT%

:: 安全计算通过率 - 避免除零和百分比格式化问题
if %TOTAL_TESTS% GTR 0 (
    echo Success rate: %PASS_COUNT% out of %TOTAL_TESTS% tests
) else (
    echo Success rate: N/A (no tests executed)
)

if %ERROR_OCCURRED% EQU 1 (
    echo Errors occurred during testing, but all executable tests were completed
) else (
    echo All tests completed successfully
)

echo Detailed log saved to: %LOG_FILE%

echo ============================== >> %LOG_FILE%
echo TEST SUMMARY: >> %LOG_FILE%
echo Total tests: %TOTAL_TESTS% >> %LOG_FILE%
echo Passed: %PASS_COUNT% >> %LOG_FILE%
echo Failed: %FAIL_COUNT% >> %LOG_FILE%

if %TOTAL_TESTS% GTR 0 (
    echo Success rate: %PASS_COUNT% out of %TOTAL_TESTS% tests >> %LOG_FILE%
) else (
    echo Success rate: N/A (no tests executed) >> %LOG_FILE%
)

if %ERROR_OCCURRED% EQU 1 (
    echo Errors occurred during testing, but all executable tests were completed >> %LOG_FILE%
) else (
    echo All tests completed successfully >> %LOG_FILE%
)

:: 清理临时文件
:cleanup
del "%TEMP_RESPONSE%" >nul 2>&1
del "%TEMP_HEADERS%" >nul 2>&1

echo.
echo Press any key to exit...
pause >nul
goto :eof

:: 主函数
:main
call :start_tests
goto :generate_report 