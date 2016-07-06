import { extend } from './utils';

const DEFAULT_BASE_URL = '/fixtures';
const DEFAULT_RESET_URL = '/_ah/yawp/datastore/delete_all';
const DEFAULT_LAZY_PROPERTIES = ['id']; // needed till harmony proxies

export default (request) => {

    class Fixtures {
        constructor() {
            this.baseUrl = DEFAULT_BASE_URL;
            this.resetUrl = DEFAULT_RESET_URL;
            this.lazyProperties = DEFAULT_LAZY_PROPERTIES;
            this.promise = null;
            this.fixtures = [];
            this.lazy = {};
        }

        config(callback) {
            callback(this);
        }

        reset() {
            return request(this.resetUrl, {
                method: 'GET'
            }).then(() => {
                this.clear();
            });
        }

        clear(all) {
            this.promise = null;
            for (let {name, path} of this.fixtures) {
                this.bindFixture(name, path);
                all && this.bindLazy(name, path);
            }
        }

        bind(name, path) {
            this.fixtures.push({name, path});
            this.bindFixture(name, path);
            this.bindLazy(name, path);
        }

        bindFixture(name, path) {
            this[name] = new Fixture(this, name, path).api;
        }

        bindLazy(name, path) {
            this.lazy[name] = new Lazy(this, name, path).api;
        }

        chain(promiseFn) {
            if (!this.promise) {
                this.promise = promiseFn();
            } else {
                this.promise = this.promise.then(promiseFn)
            }
            return this.promise;
        }

        load(callback) {
            if (!this.promise) {
                callback();
                return;
            }
            this.promise.then(() => {
                callback();
            });
        }

        getLazyDataFor(name, key) {
            var lazy = this.lazy[name].self;
            return lazy.getData(key);
        }

    }

    class Fixture {
        constructor(fx, name, path) {
            this.fx = fx;
            this.name = name;
            this.path = path;
            this.api = this.createApi();
        }

        createApi() {
            return (key, data) => {
                return this.fx.chain(this.load(key, data));
            };
        }

        url() {
            return this.fx.baseUrl + this.path;
        }

        load(key, data) {
            this.createStubs(key);
            return this.createLoadPromiseFn(key, data);
        }

        createLoadPromiseFn(key, data) {
            if (!data) {
                data = this.getLazyDataFor(key);
            }

            return () => {
                if (this.isLoaded(key)) {
                    return this.api[key];
                }

                return this.prepare(data).then((object) =>
                    request(this.url(), {
                        method: 'POST',
                        json: true,
                        body: JSON.stringify(object)
                    }).then((response) => {
                        this.api[key] = response;
                        return response;
                    }));
            }
        }

        getLazyDataFor(key) {
            var lazy = this.fx.lazy[this.name].self;
            return lazy.getData(key);
        }

        prepare(data) {
            return new Promise((resolve) => {
                let object = {};
                extend(object, data);

                for (let key of Object.keys(object)) {
                    let value = object[key];
                    if (value instanceof Function) {
                        value().then((actualValue) => {
                            object[key] = actualValue;
                        });
                    }
                    // deep into the object
                }
                return resolve(object);
            });
        }

        createStubs(key) {
            if (this.hasStubs(key)) {
                return;
            }
            let self = this;
            this.api[key] = this.fx.lazyProperties.reduce((map, property) => {
                map[property] = () => {
                    return new Promise((resolve) => resolve(self.api[key][property]));
                }
                return map;
            }, {});
            this.api[key].__stub__ = true;
        }

        isLoaded(key) {
            return this.api[key] && !this.hasStubs(key);
        }

        hasStubs(key) {
            return this.api[key] && this.api[key].__stub__;
        }
    }

    class Lazy {
        constructor(fx, name, path) {
            this.fx = fx;
            this.name = name;
            this.path = path;
            this.data = {};
            this.api = this.createApi();
        }

        createApi() {
            let api = (key, data) => {
                this.createLazyStubs(key);
                this.data[key] = data;
            };
            api.self = this;
            return api;
        }

        getData(key) {
            return this.data[key];
        }

        createLazyStubs(key) {
            if (this.hasStubs(key)) {
                return;
            }
            this.api[key] = this.fx.lazyProperties.reduce((map, property) => {
                map[property] = () => {
                    return this.fx[name].load(key).then(() => this.fx[name][key][property]);
                };
                return map;
            }, {});
            this.api[key].__stub__ = true;
        }

        hasStubs(key) {
            return this.api[key] && this.api[key].__stub__;
        }
    }

    return new Fixtures();
}