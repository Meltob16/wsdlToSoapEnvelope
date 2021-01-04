import {Component} from 'react';
import axios from 'axios';
import qs from 'qs';

export class WsdlForm extends Component {

    constructor(props) {
        super(props);
        this.state = {
            value: 'Insert WSDL',
            response: ''
        };


        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleChange(event) {
        this.setState({value: event.target.value});
    }

    handleSubmit(event) {
        event.preventDefault();

        const axios = require('axios');

        axios
            .put(
                "http://localhost:8080/wsdlToTemplate",
                this.state.value,
                {headers: {"Content-Type": "text/plain"}}
            )
            .then(r => {
                this.setState({response: r.data});
            })
            .catch(e => console.log(e));
    }


    render() {
        const { operations } = this.state
        return (

            <form onSubmit={this.handleSubmit}>
                <div>
                list of operations {
                operations.length ?
                operations.map(operation => <div> {operation.response} </div>) :
                null
            }
                </div>

                <label>
                    <textarea value={this.state.value} onChange={this.handleChange}/>
                </label>
                <input type="submit" value="Submit WSDL" className="button"/>
            </form>

        );
    }
}

export default WsdlForm
