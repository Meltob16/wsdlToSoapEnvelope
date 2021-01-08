import React from "react";
import styled, { keyframes } from "styled-components";

const fadeIn = keyframes`
  from {
    opacity: 0;
    transform: translateX(-300px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
`;

const StyledDiv = styled.div`
  animation: ${fadeIn} 4s ease;
  transition: visibility 4s ease;
`;

const FadeIn = ({ children }) => {
  return <StyledDiv>{children}</StyledDiv>;
};

export default FadeIn;