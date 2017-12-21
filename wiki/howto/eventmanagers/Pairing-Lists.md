#Pairing list
This page provides some useful information about pairing lists. It is divided in a general part where the setup of a generation process is explained and a part that provides additional functions.

##General

A pairing list provides basically the associations of competitor and boat in every single racecolumn of their regatta. 
It is highly important that the competitors sail as frequent as possible against each other. In addition every competitor should sail as frequent as possible on every boat. These two constraints are considered by the generation process. 

###How to create a pairing list 
First of all you need a regatta with at least one series with fleets and races that is not a medal series and registered competitors and boats.  

Now follow these steps:  
**Open Adminconsole > Select 'Leaderboards' on the left menu > Enter your leaderboard name in the Leaderboards tab > Click on the 'Pairing List' Button inside the 'Actions' Column:**  

[Screenshot 1]

Right now the setup dialog appears:

[screenshot 2]

You can specify the number of competitors, flight multiplier and which series should be used.  
**(Note! When changing the standard count of competitors you can not apply your generated Pairing list to racelogs or print it. For adding/removing competitors please use the register function!)**  
For more information about flight multiplier please read below.  
When you hit the **'ok'** button a small progress dialog appears and after a few seconds a new window will be opened:

[screenshot 3]

In this window there are some attributes of the pairing list in the left box:  
* Number of Flights: This number corresponds to the count of all Races in your selected series  
* Number of Groups: It describes the number of fleets in one single Flight  
* Number of Competitors: This is equal to the count of all registered competitors on this leaderboard  
* Quality: This value describes the quality of one pairing list template (for more information about quality please read below)  
On the right side a box with the title **'Pairinglist Template'** is displayed. If there are registered competitors every different value in this box will be replaced with one competitor. The matrix describes the assignments of each competitor to a boat where each row represents a single fleet and each column a single boat.  
When you hit **'Apply to Racelogs'** the competitor to boat assignments will be registered into racelogs. In the print preview the assignment of all competitors to boats are displayed. For more information about the print function and the csv export please read below.

###Flight Multiplier
In some cases it is necessary to reduce the transfer traffic while an event (e.g. if only electrical motorboats can be used). In this case you can use the flight multiplier. It will duplicate flights as often as given (eg.: flightMultiplier equals two means that a Flight occurs two times). Furthermore it will set the "Fleets Can Run In Parallel"-flag, so you can run the first fleets of the repeated flights in a straight row without any changes. If you use the flight multiplier a smaller template is calculated and duplicated, because of this the quality can be worse than normal.   
**(Note! If there is more than one series selected the flight multiplier can not be used!)**

###Quality
The quality of a pairinglist is measured by the standard deviation of its template. The main aim of a pairinglist is that every competitor competes against every other competitor equal times. So if all counts of matches against the other competitors are equal it would be a "perfect" pairinglist with a quality value of 0. The standard deviation which is used as quality describes the average deviation from this "perfect" state, so a very low number is better than a high number. The average quality of a pairinglist varies for different numbers of flights and fleets and competitors. If you think the quality is too bad, try to hit the refresh button and a new template with a different quality will be generated. 

##Additional functions

###CSV-Export
This function can be used to put the numbers from the generated Pairing list template into Excel. After generating the template you can hit a link called **'Export as CSV'** and a file called **'pairingListTemplate.csv'** will be downloaded.  
Now follow these steps:  
**Open a new or existing Excel file > In your table select the cell in the top left corner of the excerpt in which you want to place your template in > On the top header bar select the 'Data' tab > From the selection underneath select 'From Text' > Switch to your download directory (Default path: C:\Users\[username]\Downloads) and open your downloaded template file > On the appeared window click 'Next' > Under Delimiters select 'Comma' and hit 'Next' and confirm with 'Finish' > On the next appeared window hit 'Properties', select 'Overwrite existing cells with new data...' and confirm twice**  
Each value should now be in a single cell.

###Print function
This function can be used to print your final Pairing list as paper or PDF document.  
**(Note! We highly recommend to use Chrome because you can setup various layout settings (In Firefox that does not work!):**  
* Layout: Orientation of the page  
* Paper size: various formats of paper sizes  
* Margins: Space on the pages borders (It is recommended to set this to 'Default' because otherwise the printer may cut some content of the page)  
* Background graphics: Enables a beautiful background styling)  

After selecting your leaderboard in the 'Leaderboards' section hit the 'Print' button under Actions:  
**(Note! You can only print a Pairing list if you applied one to your Racelogs!)**

[Screenshot_Printing_1]

A new tab displaying the pairing list table opens. Now hit 'Print' and the browsers print dialog will open:  

[Screenshot_Printing_2]



