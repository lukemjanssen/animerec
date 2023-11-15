import React, { useContext, useState } from "react";

const RecAnimePageContext = React.createContext();
export function useAnimeContext() {
    return useContext(RecAnimePageContext);
}

export function RecAnimePageProvider({ children }) {
    const phraseDict = {
        "idle": ['Welcome to the Anime Recommender','Never have a bad anime pick again', 'Find out your next favorite anime', 'Get yourself some great anime takes', 'For when you just finished a great anime, but want more', 'TODO: Add more phrases'],
        "loading": ['Loading your animes...', 'This process usually takes a few minutes', 'Handpicking relevant options...', 'Consulting with our anime experts', 'TODO: Add more phrases'],
        "found": ['Here\'s our picks for you:']
    }

    const [loading, setLoading] = useState(false);
    const [phrases, setPhrases] = useState(phraseDict['idle']);
    const [animeList, setAnimeList] = useState([]);

    // Function to toggle loading state
    const toggleLoading = () => {
        setLoading(!loading);
    }

    // Function to set phrases
    const selectPhrases = (phraseList) => {
        setPhrases(phraseDict[phraseList]);
    }

    const value = {
        loading,
        toggleLoading,
        phrases,
        selectPhrases,
        animeList,
        setAnimeList
    }

    return (
        <RecAnimePageContext.Provider value={value}>
            {children}
        </RecAnimePageContext.Provider>
    )
}