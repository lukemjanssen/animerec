import React from "react";
import "./bouncy.css"

// Source: https://uiball.com/ldrs/
export default function BouncyLoading(){
  return (
    <div className="container">
    <div className="cube"><div className="cube__inner"></div></div>
    <div className="cube"><div className="cube__inner"></div></div>
    <div className="cube"><div className="cube__inner"></div></div>
    </div>
  );
};
