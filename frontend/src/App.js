import BasicAppBar from './components/AppBar/BasicAppBar';
import HeaderText from './components/HeaderText/HeaderText';
import SearchBar from './components/SearchBar/SearchBar';
import Waves from './components/Waves/Waves';

import { Box } from '@mui/material';

function App() {
  return (
    <>
      <Box sx={{zIndex:10}}>
        <BasicAppBar />
        <Box sx={{mt: '5%'}}>
          <HeaderText />
          <SearchBar />
        </Box>
      </Box>
      <Box sx={{zIndex: -999}}>
        <Waves />
      </Box>
    </>
  );
}



export default App;
