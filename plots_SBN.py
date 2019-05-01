import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

#Distribution over time Tweets
data_y = pd.read_csv("distribution_y.csv")
data_y = data_y.iloc[:,:].values
data_n = pd.read_csv("distribution_n.csv")
data_n = data_n.iloc[:,:].values
data = np.append(data_y,data_n)

plt.hist(data)
plt.xlabel("Time")
plt.ylabel("Frequency")
plt.title("Distribution over time")
plt.show()

plt.hist(data_y)
plt.xlabel("Time")
plt.ylabel("Frequency")
plt.title("Distribution over time Yes")
plt.show()

plt.hist(data_n)
plt.xlabel("Time")
plt.ylabel("Frequency")
plt.title("Distribution over time No")
plt.show()


###############################################################################

#comparing the time-series of each group (Y/N)
no_one = pd.read_csv("cores_words/cores_no/core0.csv", header = None)
no_two = pd.read_csv("cores_words/cores_no/core1.csv", header = None)
yes_one = pd.read_csv("cores_words/cores_yes/core0.csv", header = None)
yes_two = pd.read_csv("cores_words/cores_yes/core1.csv", header = None)

clusters_dict = {"no":[no_one, no_two], "yes":[yes_one, yes_two]}

#for each word in each group in each cluster plot the time series
mean_time_series_dict = {}
for group in clusters_dict:
    mean_time_series_dict[group] = {}
    for i in range(len(clusters_dict[group])):
        data = clusters_dict[group][i]
        words = data.iloc[:,0].values
        mean_time_series = [0]*80
        for j in range(len(data)):
            time_series = pd.DataFrame(data.iloc[j,1:].values)
            plt.plot(time_series)
            for k in range(len(data.iloc[j,1:].values)):
                mean_time_series[k] += (1/len(data))*data.iloc[j,1:].values[k]
        mean_time_series_dict[group][group + " " + str(i)] = mean_time_series
        plt.title(group + " " + str(i))
        plt.xlabel("Time (grain 3h)")
        plt.ylabel("Frequency (Norm)")
        plt.legend(words, bbox_to_anchor = (1,1))
        plt.show()

#for each group plot the average time sereis for each cluster
for group in mean_time_series_dict:
    for cluster in mean_time_series_dict[group]:
        plt.plot(pd.DataFrame(mean_time_series_dict[group][cluster]))
    plt.title("Mean time series " + group)
    plt.xlabel("Time (grain 3h)")
    plt.ylabel("Frequency (Norm)")
    plt.legend(mean_time_series_dict[group].keys(), bbox_to_anchor = (1,1))
    plt.show()
        