import React from 'react'
import EventBus from 'vertx3-eventbus-client'

class CuteNames extends React.Component {

    constructor(props) {
        super(props);
        const eventBus = new EventBus("http://localhost:8082/eventbus");
        var _this = this;
        eventBus.enableReconnect(true);
        eventBus.onopen = function () {
            eventBus.registerHandler('cute-names', function (error, message) {
                if (error === null) {
                    console.info(message.body);
                    _this.setState({name:message.body});

                } else {
                    console.error(error, 'cute-names');
                }
            });
        };
        this.state = {
            name: 'no name'
        };
    }

    render() {
        return (
            <div className='cute'>
                <h1>Cute name</h1>
                <p>{this.state.name}</p>
            </div>
        );
    }
}

export default CuteNames
