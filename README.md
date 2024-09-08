# The Perfect Plate

**The Perfect Plate** is an Android application built using Kotlin that allows users to share restaurant recommendations, 
including photos, descriptions, and addresses. Users can also view recommendations from others, like posts, add comments, 
and manage their profile information. This project uses **Firebase Realtime Database** for storing data and **Firebase Storage** for storing images.

## Table of Contents

1. [Features](#features)
2. [Project Structure](#project-structure)
3. [Technologies Used](#technologies-used)
4. [Firebase Configuration](#firebase-configuration)
5. [Data Structure](#data-structure)
6. [Installation](#installation)
7. [Usage](#usage)

## Features

- User authentication (sign up, login) via Firebase Authentication.
- Users can add restaurant recommendations, including photos, descriptions, and adresses.
- Users can view the main feed of all recommendations.
- Users can like posts and add comments.
- Users can view detailed information about a post.
- Users can manage their profile, including a profile picture.
- Navigation bar to easily switch between main feed, profile, and recommendation uploads.

## Project Structure

- **MainActivity**: Handles user authentication (login and sign-up).
- **MainActivityApp**: Handles the main parts of the app, including the main feed, post page, upload recommendation, and profile sections.
- **Fragments**:
  - `LoginFragment` and `SignUpFragment` for authentication.
  - `MainFeedFragment` displays the main feed of posts.
  - `PostPageFragment` displays details of a single post.
  - `UploadRecommendationFragment` allows users to upload a new restaurant recommendation.
  - `ProfileFragment` displays user profile and allows log-out.

## Technologies Used

- **Kotlin** for Android development.
- **Google Firebase** to manage all the data in the project - from user authenticate to images storage.
  
## Firebase Configuration

When using the app, you will automatically connect to the shared Firebase backend and see the data available in the common database.

### Firebase Services Used

- **Firebase Authentication**: For managing user sign-up and log-in.
- **Firebase Realtime Database**: For storing data related to restaurant recommendations, users, comments, and likes.
- **Firebase Storage**: For storing images related to recommendations and user profile pictures.

## Installation

To install and run this project locally, follow these steps:

1. **Clone the repository**:
   ```bash
   git clone https://github.com/tehillaIny/the-perfect-plate.git

## Open in Android Studio

1. Open Android Studio and select **Open an existing project**.
2. Choose the folder where you cloned the repository.

## Install Dependencies

Ensure you have installed the required dependencies. Android Studio will automatically prompt you to sync Gradle and install required libraries.

## Run the App

Connect an Android device or open an emulator, then click the **Run** button in Android Studio to build and run the app.

## Usage

- **Sign Up or Log In**: Use the sign-up or log-in feature to create an account or access an existing one.
- **View Recommendations**: On the main feed, browse through restaurant recommendations.
- **Add a Recommendation**: Upload your own restaurant recommendation, including name, description, address and photos.
- **Like Posts**: Click the heart icon to like a recommendation.
- **Add Comments**: Comment on any post by typing your thoughts.
- **Manage Profile**: Go to the profile page to update your profile picture or log out.
