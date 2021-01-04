import React, { Component } from 'react'

class ListOfOperations extends Component {

     IdiomaticList(props) {
        return (
            <div>
                {props.items.map((item, index) => (
                    <Item key={index} item={item} />
                ))}
            </div>
        );
    }


    render() {
        return (
            <div>
                <form>
                    <textarea/>
                </form>
            </div>
        )
    }
}

export default WsdlPost
