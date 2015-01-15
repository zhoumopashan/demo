#!/bin/bash
git add .
git reset HEAD project.properties
git commit -m "commit"
git push origin master:master
