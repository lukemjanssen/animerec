import * as React from 'react';
import Drawer from '@mui/material/Drawer';
import List from '@mui/material/List';
import Divider from '@mui/material/Divider';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import SavedSearchIcon from '@mui/icons-material/SavedSearch';
import { Box, IconButton, Typography } from '@mui/material';
import GitHubIcon from '@mui/icons-material/GitHub';

function SideMenu({openMenu, setOpenMenu}) {
  const handleDrawerToggle = () => {
    setOpenMenu(!openMenu);
  };

  return (
    <div>
      <Drawer
        className="drawer-menu"
        variant="temporary"
        anchor="left"
        open={openMenu}
        onClose={handleDrawerToggle}
        PaperProps={{
            sx: {
              backgroundColor: "#424549",
              color: "#fff",
            }
          }}
      > 
        <List>
          <ListItem key={'search'} >
            <ListItemButton onClick={() => alert('TODO: Route')}>
                <ListItemIcon>
                    <SavedSearchIcon color='white'/>
                </ListItemIcon>
                <ListItemText primary={"Anime Recommendation Lookup"} />
                </ListItemButton>
            </ListItem>
        </List>
        <Divider color='gray'/>
        <Typography variant="p" sx={{display:'flex', justifyContent:'center', alignItems:'center', color: '#fff'}}>
            Made by two dum-dums
        </Typography>
        <Box sx={{display:'flex', justifyContent:'center'}}>
            <IconButton href="https://github.com/lukemjanssen/animerec">
                <GitHubIcon color='white'/>
            </IconButton>
        </Box>
      </Drawer>
    </div>
  );
}

export default SideMenu;
