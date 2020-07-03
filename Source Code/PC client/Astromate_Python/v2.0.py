from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import Select
from selenium.common.exceptions import NoSuchElementException
from PIL import Image
import os
import socket
import calendar

options = Options()
options.add_argument("--headless")
options.add_argument("--window-size=1920x1080")
options.add_argument('log-level=3')

driverpath =  os.path.join(os.path.split(__file__)[0] , 'drivers{0}{1}'.format(os.path.sep , "chrome_windows.exe"))
os.chmod(driverpath , 0o755 ) 
driver = webdriver.Chrome(executable_path=driverpath , options=options)

def check_exists_by_xpath(xpath):
    try:
        driver.find_element_by_xpath(xpath)
    except NoSuchElementException:
        return False
    return True

s=socket.socket()
print ("Socket successfully created")
port=12523
s.bind(('',port))
s.listen(0)
while True:
    print("Waiting for connection")
    c,addr=s.accept()
    print("Connected\n")
    msg=c.recv(5000)
    msg=msg.decode('UTF-8')
    print("Message received from phone")
    if(msg=='TEST\n'):
        print("Phone connected")
        c.close()
        continue
    print(msg)
    c.close()
    tokens=msg.split("<br>")
    name=tokens[2].partition(' ')[2]
    sex=tokens[3].split()[1]
    full_date=tokens[4].split()[1]
    full_date_tokens=full_date.split('-')
    dob=full_date_tokens[0]
    mob=list(calendar.month_abbr).index(full_date_tokens[1][0:3])
    yob=full_date_tokens[2]
    hr=tokens[5].split()[1].split(':')[0]
    min=tokens[5].split()[1].split(':')[1]
    if(tokens[5].split()[2]=="PM"):
        hr=str(int(hr)+12)
    pob=tokens[6].split()[1].split(',')[0]
    def open_browser():
        print("Opening horoscope...")
        global pob
        driver.get("http://astrosage.com")
        driver.find_element_by_id("name").send_keys(name)
        select_sex=Select(driver.find_element_by_id("sex"))
        select_sex.select_by_visible_text(sex)
        elem_day=driver.find_element_by_id("Day")
        elem_day.clear()
        elem_day.send_keys(dob)
        elem_month=driver.find_element_by_id("Month")
        elem_month.clear()
        elem_month.send_keys(mob)
        elem_year=driver.find_element_by_id("Year")
        elem_year.clear()
        elem_year.send_keys(yob)
        elem_hours=driver.find_element_by_id("hrs")
        elem_hours.clear()
        elem_hours.send_keys(hr)
        elem_mins=driver.find_element_by_id("min")
        elem_mins.clear()
        elem_mins.send_keys(min)
        elem_secs=driver.find_element_by_id("sec")
        elem_secs.clear()
        elem_secs.send_keys("0")
        driver.find_element_by_id("place").send_keys(pob)
        elem_secs.submit()
        if(driver.current_url=="http://astrosage.com/kundli/confirmdata.asp"):
            if(check_exists_by_xpath('//*[@id="UserCityDetailsForm"]/h1')):
                driver.find_element_by_xpath('//*[@id="UserCityDetailsForm"]/div/div[2]/table/tbody/tr[2]/td[1]/a').click()
            else:
                pob=input("Invalid place of birth, type a different place:")
                open_browser()
                return
        driver.find_element_by_xpath('//*[@id="main-content"]/div[1]/div[1]/a/div/div/img').click()
        driver.get_screenshot_as_file("horoscope.png")
    open_browser()
    img=Image.open("horoscope.png")
    img.show()
    print("Horoscope Opened\n")