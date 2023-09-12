# bzm-selenium
Selenium tests running on Blazemeter's Grid

## Instructions for running locally
1. Clone the repo.
2. Update .env with your Blazemeter's API_KEY and API_SECRET. (Make sure you don't push these keys as plain text)
```.env
API_KEY=f453364f583758j646
API_SECRET=f453364f583758j646f453364f583758j646f453364f583758
```
3. Run mvn test 
```sh
$ mvn test
```

## Github actions
This repo has a workflow which will be trigger every push. Just add your API_KEY and API_SECRET as Github actions secrets.