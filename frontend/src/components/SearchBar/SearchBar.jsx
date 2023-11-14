import React, { useState } from 'react';
import { TextField, Button, Box } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import InputAdornment from '@mui/material/InputAdornment';
import './SearchBar.css';

export default function SearchBar() {
    const [searchTerm, setSearchTerm] = useState('');

    const handleInputChange = (event) => {
        setSearchTerm(event.target.value);
    };

    const handleSubmit = (event) => {
        event.preventDefault();
        console.log(searchTerm);
    };

    return (
        <div className='searchBar' >
            <Box 
                component="form" 
                className={"searchBarForm"} 
                onSubmit={handleSubmit} 
                sx={{
                    display: 'flex', 
                    flexDirection: 'column', 
                    alignItems: 'center', 
                    justifyContent: 'center',
                    mt: '3%'
                }}
            >
                <TextField 
                id="search-bar" 
                variant="outlined" 
                value={searchTerm} 
                onChange={handleInputChange} 
                className='bar'
                sx={{ input: { color: '#fff' } }}
                InputProps={{
                    startAdornment: (
                    <InputAdornment position='start'>
                        <SearchIcon color='white'/>
                    </InputAdornment>
                    ),
                    sx: { 
                        border:'1px solid #99aab5',
                        borderRadius: '30px'
                    }
                }}
                />                
                <Button variant="contained" color="mal" type="submit" sx={{ mt: 2 }}>Search</Button>
            </Box>
        </div>
    );
}