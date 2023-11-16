import * as React from 'react';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import MenuIcon from '@mui/icons-material/Menu';
import malIcon from '../../assets/favicon.ico'
import SideMenu from './SideMenu';

export default function BasicAppBar() {
  const [openMenu, setOpenMenu] = React.useState(false);

  return (
    <Box sx={{ flexGrow: 1 }}>
      <SideMenu openMenu={openMenu} setOpenMenu={setOpenMenu} />
      <AppBar position="static" color='mal'>
        <Toolbar sx={{display:'flex', justifyContent:'space-between'}}>
          <IconButton
            size="large"
            edge="start"
            color="white"
            aria-label="menu"
            sx={{ mr: 2 }}
            onClick={() => setOpenMenu(!openMenu)}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" component="div" color='#fff'>
            Anime Recommendations
          </Typography>
          <img src={malIcon} alt="malIcon" />
        </Toolbar>
      </AppBar>
      <SideMenu />
    </Box>
  );
}