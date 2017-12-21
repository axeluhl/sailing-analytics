#General
This page provides some useful information about pairinglists and the calculation of pairinglists.

###Quality
The quality of a pairinglist is measured by the standard deviation of its template. The main aim of a pairinglist is that every competitor competes against every other competitor equal times. So if all counts of matches against the other competitors are equal it would be a "perfect" pairinglist. The standard deviation which is used as quality describes the average deviation from this "perfect" state, so a very low number is better than a high number. The average quality of a pairinglist varies for different numbers of flights and fleets and competitors. If you think the quality is too bad, try to hit the refresh button and get a new template. 

###Flight Multiplier
In some cases it is necessary to reduce the transfer traffic while an event (e.g. if only electrical motorboats can be used). In this case you can use the flight multiplier. It will duplicate flights as often as given. In other words: the flights are repeated. Furthermore it will set the "Fleets Can Run In Parallel"-flag, so you can run the first fleets of the repeated flights in a straight row without any changes. If you use the flight multiplier a smaller template is calculated and duplicated, because of this the quality can be worse than normal.

###CSV-Export
If you want to use a csv-file to export a pairinglist into excel you have to download it by clicking the link. After that you have t> o follow these steps:

1. Go to the first cell of the space, where your template should be imported to.
2. Click on the "Data" tab of excel.
3. Click "From Text" and choose the csv-file, which you downloaded.
4.Now there should be a "Text import Wizard" window. You do not have to change anything in the first step and just click "Next>". 
5. In the second window you have to change "Delimiters" to "Comma" and click "Next>".
6. In the third step you do not have anything to change. Just click "Finish".
7. There should be a "Import Data" window. Click the "Properties..." button.
8. Check "Overwrite existing cells with new data, clear unused cells"
9. Click "OK".
10. Back in the "Import Data" window, click "OK".

Your data should be imported. You can adjust the columns to get good formatting.                

#Pairing Lists
This page provides some useful information about pairing lists. It is divided in a general part where the setup of a generation process is explained and a part that provides additional functions.

##General

A pairing list provides basically the associations of competitor and boat in every single racecolumn of their regatta. 
It is highly important that the competitors sail as frequent as possible against each other. In addition every competitor should sail as frequent as possible on every boat. These two constraints are considered by the generation process. 

###How to create a Pairing List 
First of all you need a regatta with at least one series with fleets and races that is not a medal series and registered competitors and boats.  

Now follow these steps:  
**Open Adminconsole > Select 'Leaderboards' on the left menu > Enter your leaderboard name in the Leaderboards tab > Click on the 'Pairing List' Button inside the 'Actions' Column:**  

[Screenshot 1]

Right now the setup dialog appears:

[screenshot 2]

You can specify the number of competitors, flight multiplier and which series should be used.  
**(Note! When changing the standard count of competitors you can not apply your generated Pairing list to Racelogs or print it. For adding/removing competitors please use the register function!)**  
For more information about flight multiplier please read below.  
When you hit the **'ok'** button a small progress dialog appears and after a few seconds a new window will be opened:

[screenshot 3]

In this window there are some attributes of the Pairing list in the left box:  
* Number of Flights: This number corresponds to the count of all Races in your selected series  
* Number of Groups: It describes the number of Fleets in one single Race  
* Number of Competitors: This is equal to the count of all registered Competitors on this Leaderboard  
* Quality: This value describes the quality of one Pairing list Template (for more information about Quality please read below)  
On the right side a box with the title **'Pairinglist Template'** is displayed. If there are registered competitors every different value in this matrix will be replaced with one competitor. The matrix describes the assignments of each Competitor to a boat where each row represents a single fleet and each column a single boat.  
When you hit **'Apply to Racelogs'** the Competitor to boat assignments will be registered into Racelogs. In the print preview the assignment of all Competitors to Boats are displayed. For more information about the print function and the csv export please read below.

###Flight Multiplier
In some cases it is necessary to reduce the transfer traffic while an event (e.g. if only electrical motorboats can be used). In this case you can use the flight multiplier. It will duplicate flights as often as given (eg.: flightMultiplier equals two means that a Flight occurs two times). Furthermore it will set the "Fleets Can Run In Parallel"-flag, so you can run the first fleets of the repeated flights in a straight row without any changes. If you use the flight multiplier a smaller template is calculated and duplicated, because of this the quality can be worse than normal.   
**(Note! If there is more than one Series selected the Flight Multiplier can not be used!)**

###Quality
The quality of a pairinglist is measured by the standard deviation of its template. The main aim of a pairinglist is that every competitor competes against every other competitor equal times. So if all counts of matches against the other competitors are equal it would be a "perfect" pairinglist with a quality value of 0. The standard deviation which is used as quality describes the average deviation from this "perfect" state, so a very low number is better than a high number. The average quality of a pairinglist varies for different numbers of flights and fleets and competitors. If you think the quality is too bad, try to hit the refresh button and a new template with a different quality will be generated. 

#Additional functions




